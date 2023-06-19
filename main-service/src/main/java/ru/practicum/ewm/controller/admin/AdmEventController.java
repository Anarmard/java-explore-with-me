package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.service.event.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdmEventController {
    private final EventService eventService;

    // Поиск событий
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventListByAdmin(@RequestParam(name = "users", required = false) List<Long> userIdList,
                                                  @RequestParam(required = false) List<String> states,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) String rangeStart,
                                                  @RequestParam(required = false) String rangeEnd,
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  HttpServletRequest request) {
        return eventService.getEventsByAdmin(userIdList, states, categories, rangeStart, rangeEnd, from, size, request);
    }

    // редактирование данных события и его статуса
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        return eventService.updateEventByAdmin(eventId, updateEventAdminRequest);
    }
}