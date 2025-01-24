package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import java.util.List;

//Закрытый API для работы с событиями
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;

    //Получение событий, добавленных текущим пользователем
    @GetMapping("/{userId}/events")
    public List<EventFullDto> getEvents(@PathVariable Long userId,
                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                  @Positive @RequestParam(defaultValue = "10") Integer size) {

        log.info("/users/{userId}/events&... PrivateEventController.getEvents(userId {}, from {}, size {}):", userId, from, size);
        return eventService.getEvents(userId, from, size);
    }

    //Добавление нового события
    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED) //201
    public EventFullDto addEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
        log.info("/users/{userId}/events PrivateEventController.addEvent(userId {}, newEventDto {})", userId, newEventDto);
        return eventService.addEvent(userId, newEventDto);
    }

    //Получение полной информации о событии добавленном текущим пользователем
    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("/users/{userId}/events/{eventId} PrivateEventController.getEvent(userId {}, eventId {})", userId, eventId);
        return eventService.getEvent(userId, eventId);
    }

    //Изменение события добавленного текущим пользователем
    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(HttpStatus.OK) //200
    public EventFullDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("/users/{userId}/events/{eventId} PrivateEventController.updateEvent(userId {},eventId {}, updateEventAdminRequest {}):", userId, eventId, updateEventUserRequest);
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    //Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("/users/{userId}/events/{eventId}/requests PrivateEventController.getEventParticipants(userId {}, eventId {})", userId, eventId);
        return eventService.getEventParticipants(userId, eventId);
    }

    //Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK) //200
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest obj) {

        log.info("/users/{userId}/events/{eventId}/requests PrivateEventController.changeRequestStatus(userId {}, eventId {}, EventRequestStatusUpdateRequest {}):", userId, eventId, obj);
        return eventService.changeRequestStatus(userId, eventId, obj);
    }

}
