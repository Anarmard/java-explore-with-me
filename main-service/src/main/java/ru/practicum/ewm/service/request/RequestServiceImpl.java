package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.enums.RequestStatus;
import ru.practicum.ewm.enums.RequestStatusUpdate;
import ru.practicum.ewm.errorHandler.exceptions.AlreadyExistsException;
import ru.practicum.ewm.errorHandler.exceptions.NotFoundException;
import ru.practicum.ewm.errorHandler.exceptions.ValidationException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    // private
    // Получение инфо о запросах на участие в событии текущего пользователя
    @Override
    public List<ParticipationRequestDto> getRequestsByCurrentUserOfCurrentEvent(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User does not exist " + userId);
        }
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event does not exist " + eventId);
        }
        List<Request> requestList = requestRepository.findAllRequestsForEventInitiator(userId, eventId);
        return requestMapper.toParticipationRequestDtoList(requestList);
    }

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @Override
    public EventRequestStatusUpdateResult updateRequest(Long userId, Long eventId,
                                                        EventRequestStatusUpdateRequest eventRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User does not exist " + userId);
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event does not exist " + eventId));

        if (event.getParticipantLimit() == 0 && !event.getRequestModeration()) {
            throw new ValidationException("Moderation is not required " + eventId);
        }

        if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ValidationException("Participation limit exceed " + eventId);
        }

        // получаем список всех запросов
        List<Long> requestIdList = eventRequest.getRequestIds();
        // получаем статус события
        RequestStatusUpdate status = eventRequest.getStatus();

        List<Request> requestList = requestIdList.stream().map((id) -> requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request does not exist "
                        + id))).collect(Collectors.toList());

        List<Request> confirmedRequests = new ArrayList<>();
        List<Request> rejectedRequests = new ArrayList<>();

        List<Request> updatedRequests = new ArrayList<>();

        // перебираем все запросы
        for (Request currentRequest : requestList) {
            if (status == RequestStatusUpdate.CONFIRMED && currentRequest.getStatus().equals(RequestStatus.PENDING)) {
                if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                    // всем отказываем когда превышен лимит
                    currentRequest.setStatus(RequestStatus.REJECTED);
                    updatedRequests.add(currentRequest);
                    rejectedRequests.add(currentRequest);
                }
                currentRequest.setStatus(RequestStatus.CONFIRMED);
                updatedRequests.add(currentRequest);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmedRequests.add(currentRequest);
            }
            if (status == RequestStatusUpdate.REJECTED && currentRequest.getStatus().equals(RequestStatus.PENDING)) {
                // вcем отказываем когда событие отменилось
                currentRequest.setStatus(RequestStatus.REJECTED);
                updatedRequests.add(currentRequest);
                rejectedRequests.add(currentRequest);
            }
        }

        // сохранили все запросы с новыми статусами в БД
        requestRepository.saveAll(updatedRequests);
        eventRepository.save(event);

        // переводим в ДТО и на выход
        List<ParticipationRequestDto> confirmedRequestsDto = requestMapper.toParticipationRequestDtoList(confirmedRequests);
        List<ParticipationRequestDto> rejectedRequestsDto = requestMapper.toParticipationRequestDtoList(rejectedRequests);

        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult();
        updateResult.setRejectedRequests(confirmedRequestsDto);
        updateResult.setConfirmedRequests(rejectedRequestsDto);

        return updateResult;
    }


    // Получение инфо о заявках текущего пользователя на участие в чужих событиях
    @Override
    public List<ParticipationRequestDto> getRequestsByCurrentUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User does not exist");
        }
        List<Request> requestList = requestRepository.findAllByRequesterIdAndNotInitiator(userId);
        return requestMapper.toParticipationRequestDtoList(requestList);
    }

    // Добавление запроса от текущего пользователя на участие в событии
    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {

        // выгружаем данные пользователя, кто отправиляет запрос
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User does not exist " + userId));

        // выгружаем данные события
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event does not exist " + eventId));

        // создаем запрос
        Request request = new Request(LocalDateTime.now(), event, requester, RequestStatus.PENDING);

        Optional<Request> requestFromDb = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (requestFromDb.isPresent()) {
            throw new AlreadyExistsException("Request already exist: userId {}, eventId {} " + userId + eventId);
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Initiator could not be requester " + userId);
        }
        if (!(event.getState().equals(EventState.PUBLISHED))) {
            throw new ValidationException("Event has not published yet");
        }

        Long limit = event.getParticipantLimit();

        // если есть ограничение, то проверяем. Если ограничения нет, то автоматически подтверждаем запрос
        if (limit != 0) {
            if (limit.equals(event.getConfirmedRequests())) {
                throw new ValidationException("Max confirmed requests was reached: " + limit);
            }
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        // если модерация не нужна, то автоматом подтверждаем запрос и увеличиваем счетчик
        if (!event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(request);
    }

    // Отмена своего запроса на участие в событии
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id and/or requester id does not exist" + requestId + userId));
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(request);
    }
}