package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.ewm.service.request.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {
    private final EventService eventService;
    private final RequestService requestService;

    // получение событий текущего пользователя
    @GetMapping
    public List<EventShortDto> getEventsByInitiator(@PathVariable Long userId,
                                                    @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                    @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("PrivateEventController / getEventsByInitiator: получение событий текущего пользователя "
                + userId + from + size);
        return eventService.getEventsByInitiator(userId, PageRequest.of(from, size));
    }

    // добавление нового события
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto newEventDto) {
        log.info("PrivateEventController / addEvent: добавление нового события " + userId + newEventDto);
        return eventService.addEvent(userId, newEventDto);
    }

    // полная инфо о событии добавленное текущим пользователем
    @GetMapping("/{eventId}")
    public EventFullDto getEventByInitiator(@PathVariable Long userId,
                                            @PathVariable Long eventId) {
        log.info("PrivateEventController / getEventByInitiator: полная инфо о событии добавленное текущим пользователем " +
                userId + eventId);
        return eventService.getEventByInitiator(userId, eventId);
    }

    // изменения события добавленного текущим пользователем
    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByInitiator(@PathVariable Long userId,
                                               @PathVariable Long eventId,
                                               @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("PrivateEventController / updateEventByInitiator: изменения события добавленного текущим пользователем " +
                userId + eventId + updateEventUserRequest);
        return eventService.updateEventByInitiator(userId, eventId, updateEventUserRequest);
    }

    // Получение инфо о запросах на участие в событии текущего пользователя
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByCurrentUserOfCurrentEvent(@PathVariable Long userId,
                                                                                @PathVariable Long eventId) {
        log.info("PrivateEventController / getRequestsByCurrentUserOfCurrentEvent: " +
                "Получение инфо о запросах на участие в событии текущего пользователя " +
                userId + eventId);
        return requestService.getRequestsByCurrentUserOfCurrentEvent(userId, eventId);
    }

    // Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequest(@PathVariable Long userId,
                                                        @PathVariable Long eventId,
                                                        @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("PrivateEventController / updateRequest: " +
                "Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя " +
                userId + eventId + eventRequestStatusUpdateRequest);
        return requestService.updateRequest(userId, eventId, eventRequestStatusUpdateRequest);
    }
}