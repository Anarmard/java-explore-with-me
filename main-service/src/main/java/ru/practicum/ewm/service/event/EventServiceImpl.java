package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.SortValue;
import ru.practicum.ewm.enums.StateActionAdmin;
import ru.practicum.ewm.enums.StateActionUser;
import ru.practicum.ewm.errorHandler.exceptions.NotFoundException;
import ru.practicum.ewm.errorHandler.exceptions.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.LocationRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    private final StatsClient statsClient;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // private
    // получение событий текущего пользователя
    @Override
    public List<EventShortDto> getEventsByInitiator(Long userId, Pageable pageable) {
        return eventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, pageable).toList());
    }

    // добавление нового события
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User does not exist " + userId));

        validateTime(newEventDto.getEventDate());

        // формируем event для сохранения в БД
        Event eventToSave = eventMapper.toEvent(newEventDto);
        eventToSave.setState(EventState.PENDING);
        eventToSave.setCreatedOn(LocalDateTime.now());

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category does not exist"));
        eventToSave.setCategory(category);
        eventToSave.setInitiator(user);
        eventRepository.save(eventToSave);
        return eventMapper.toEventFullDto(eventToSave);
    }

    // полная инфо о событии добавленное текущим пользователем
    @Override
    public EventFullDto getEventByInitiator(Long userId, Long eventId) {
        return eventMapper.toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event does not exist")));
    }


    // изменения события добавленного текущим пользователем
    @Override
    public EventFullDto updateEventByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event does not exist " + eventId));
        if (eventToUpdate.getState().equals(EventState.CANCELED) || eventToUpdate.getState().equals(EventState.PENDING)) {
            if (updateEventUserRequest.getEventDate() != null
                    && updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Дата и время на которые намечено событие не может быть раньше, " +
                        "чем через два часа от текущего момента ");
            }
            if (StateActionUser.SEND_TO_REVIEW == updateEventUserRequest.getStateAction()) {
                eventToUpdate.setState(EventState.PENDING);
            }
            if (StateActionUser.CANCEL_REVIEW == updateEventUserRequest.getStateAction()) {
                eventToUpdate.setState(EventState.CANCELED);
            }
        } else {
            throw new ValidationException("State of event should be CANCELED or PENDING" + eventToUpdate.getState());
        }

        updateEventEntity(updateEventUserRequest, eventToUpdate);
        eventRepository.save(eventToUpdate);
        return eventMapper.toEventFullDto(eventToUpdate);
    }

    // admin
    // поиск событий
    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> userIdList, List<String> states, List<Long> categories,
                                               String rangeStart, String rangeEnd, Integer from, Integer size) {
        int page = from / size;
        final PageRequest pageRequest = PageRequest.of(page, size);
        if (states == null & rangeStart == null & rangeEnd == null) {
            return eventRepository.findAll(pageRequest)
                    .stream()
                    .map(eventMapper::toEventFullDto)
                    .collect(Collectors.toList());
        }

        List<EventState> stateList = states.stream().map(EventState::valueOf).collect(Collectors.toList());

        LocalDateTime start;
        if (rangeStart != null && !rangeStart.isEmpty()) {
            start = LocalDateTime.parse(rangeStart, DTF);
        } else {
            start = LocalDateTime.now().plusYears(5);
        }

        LocalDateTime end;
        if (rangeEnd != null && !rangeEnd.isEmpty()) {
            end = LocalDateTime.parse(rangeEnd, DTF);
        } else {
            end = LocalDateTime.now().plusYears(5);
        }

        if (userIdList.size() != 0 && states.size() != 0 && categories.size() != 0) {
            return findEventDtoWithAllParameters(userIdList, categories, pageRequest, stateList, start, end);
        }
        if (userIdList.size() == 0 && categories.size() != 0) {
            return findEventDtoWithAllParameters(userIdList, categories, pageRequest, stateList, start, end);
        } else {
            return new ArrayList<>();
        }
    }

    // редактирование данных события и его статуса
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event does not exist " + eventId));
        if (updateEventAdminRequest.getEventDate() != null) {
            validateTime(updateEventAdminRequest.getEventDate());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == StateActionAdmin.PUBLISH_EVENT) {
                if (eventToUpdate.getState().equals(EventState.PENDING)) {
                    eventToUpdate.setState(EventState.PUBLISHED);
                    eventToUpdate.setPublishedOn(LocalDateTime.now());
                } else {
                    throw new ValidationException("Event should be PENDING in order to be PUBLISHED" +
                            updateEventAdminRequest.getStateAction());
                }
            }
            if (updateEventAdminRequest.getStateAction() == StateActionAdmin.REJECT_EVENT) {
                if (eventToUpdate.getState().equals(EventState.PUBLISHED)) {
                    throw new ValidationException("Event should be PENDING in order to reject it " +
                            updateEventAdminRequest.getStateAction());
                }
                eventToUpdate.setState(EventState.CANCELED);
            }
        }
        updateEventEntity(updateEventAdminRequest, eventToUpdate);

        eventRepository.save(eventToUpdate);
        return eventMapper.toEventFullDto(eventToUpdate);
    }

    // public
    // получение событий с возможностью фильтрации
    @Override
    public List<EventShortDto> getEventList(String text, List<Long> categoryIdList, Boolean paid, String rangeStart,
                                            String rangeEnd, Boolean onlyAvailable, SortValue sort, Integer from,
                                            Integer size, String userIp, String requestUri) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (rangeStart != null && rangeEnd != null) {
            start = LocalDateTime.parse(rangeStart, DTF);
            end = LocalDateTime.parse(rangeEnd, DTF);
            if (start.isAfter(end)) {
                throw new ValidationException("Wrong dates");
            }
        } else {
            if (rangeStart == null && rangeEnd == null) {
                start = LocalDateTime.now();
                end = LocalDateTime.now().plusYears(10);
            } else {
                if (rangeStart == null) {
                    start = LocalDateTime.now();
                }
                if (rangeEnd == null) {
                    end = LocalDateTime.now();
                }
            }
        }

        final PageRequest pageRequest = PageRequest.of(from / size, size,
                Sort.by(SortValue.EVENT_DATE.equals(sort) ? "eventDate" : "views"));
        List<Event> eventEntities = eventRepository.searchPublishedEvents(categoryIdList, paid, start, end, pageRequest)
                .getContent();
        statsClient.hit(userIp, requestUri);

        if (eventEntities.isEmpty()) {
            return Collections.emptyList();
        }

        java.util.function.Predicate<Event> eventEntityPredicate;
        if (text != null && !text.isEmpty()) {
            eventEntityPredicate = eventEntity -> eventEntity.getAnnotation().toLowerCase().contains(text.toLowerCase())
                    || eventEntity.getDescription().toLowerCase().contains(text.toLowerCase());
        } else {
            eventEntityPredicate = eventEntity -> true;
        }

        return eventEntities.stream().map(eventMapper::toEventShortDto).collect(Collectors.toList());
    }

    // получение подробной инфо о событии по его id
    @Override
    public EventFullDto getEvent(Long eventId, String userIp, String requestUri) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event does not exist " + eventId));

        statsClient.hit(userIp, requestUri);

        return eventMapper.toEventFullDto(event);
    }

    private void validateTime(LocalDateTime start) {
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала события должна быть не ранее чем за час от даты публикации");
        }
    }

    private void updateEventEntity(UpdateEventUserRequest event, Event eventToUpdate) {
        eventToUpdate.setAnnotation(Objects.requireNonNullElse(event.getAnnotation(), eventToUpdate.getAnnotation()));
        eventToUpdate.setCategory(event.getCategory() == null
                ? eventToUpdate.getCategory()
                : categoryRepository.findById(event.getCategory()).orElseThrow(() -> new NotFoundException("Category not fount")));
        eventToUpdate.setDescription(Objects.requireNonNullElse(event.getDescription(), eventToUpdate.getDescription()));
        eventToUpdate.setEventDate(Objects.requireNonNullElse(event.getEventDate(), eventToUpdate.getEventDate()));
        eventToUpdate.setLocation(event.getLocation() == null
                ? eventToUpdate.getLocation()
                : locationRepository.findByLatAndLon(event.getLocation().getLat(), event.getLocation().getLon())
                .orElse(new Location(null, event.getLocation().getLat(), event.getLocation().getLon())));
        eventToUpdate.setPaid(Objects.requireNonNullElse(event.getPaid(), eventToUpdate.getPaid()));
        eventToUpdate.setParticipantLimit(Objects.requireNonNullElse(event.getParticipantLimit(), eventToUpdate.getParticipantLimit()));
        eventToUpdate.setRequestModeration(Objects.requireNonNullElse(event.getRequestModeration(), eventToUpdate.getRequestModeration()));
        eventToUpdate.setTitle(Objects.requireNonNullElse(event.getTitle(), eventToUpdate.getTitle()));
    }

    private List<EventFullDto> findEventDtoWithAllParameters(List<Long> userIds, List<Long> categories,
                                                             PageRequest pageRequest, List<EventState> stateList,
                                                             LocalDateTime start, LocalDateTime end) {
        Page<Event> eventsWithPage = eventRepository.findAllWithAllParameters(userIds, stateList, categories, start, end,
                pageRequest);

        return eventsWithPage.stream().map(eventMapper::toEventFullDto).collect(Collectors.toList());
    }

    private void updateEventEntity(UpdateEventAdminRequest event, Event eventToUpdate) {
        eventToUpdate.setAnnotation(Objects.requireNonNullElse(event.getAnnotation(), eventToUpdate.getAnnotation()));
        eventToUpdate.setCategory(event.getCategory() == null
                ? eventToUpdate.getCategory()
                : categoryRepository.findById(event.getCategory()).orElseThrow(() -> new NotFoundException("Category not fount")));
        eventToUpdate.setDescription(Objects.requireNonNullElse(event.getDescription(), eventToUpdate.getDescription()));
        eventToUpdate.setEventDate(Objects.requireNonNullElse(event.getEventDate(), eventToUpdate.getEventDate()));
        eventToUpdate.setLocation(event.getLocation() == null
                ? eventToUpdate.getLocation()
                : locationRepository.findByLatAndLon(event.getLocation().getLat(), event.getLocation().getLon())
                .orElse(new Location(null, event.getLocation().getLat(), event.getLocation().getLon())));
        eventToUpdate.setPaid(Objects.requireNonNullElse(event.getPaid(), eventToUpdate.getPaid()));
        eventToUpdate.setParticipantLimit(Objects.requireNonNullElse(event.getParticipantLimit(), eventToUpdate.getParticipantLimit()));
        eventToUpdate.setRequestModeration(Objects.requireNonNullElse(event.getRequestModeration(), eventToUpdate.getRequestModeration()));
        eventToUpdate.setTitle(Objects.requireNonNullElse(event.getTitle(), eventToUpdate.getTitle()));
    }
}