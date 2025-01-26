package ru.practicum.ewm.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.service.EventService;
import java.time.LocalDateTime;
import java.util.List;

//API для работы с событиями
@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    //Поиск событий
    @GetMapping
    public List<EventFullDto> getEvents_2(@RequestParam(required = false) List<Long> users,
                                          @RequestParam(required = false) List<State> states,
                                          @RequestParam(required = false) List<Long> categories,
                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                          @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                          @Positive @RequestParam(defaultValue = "10") Integer size) {

        log.info("/admin/events&...  AdminEventController.getEvents_2(users {}, states {}, categories {}, rangeStart {}, rangeEnd {}, from {}, size {})", users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getEvents_2(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    //Изменение события
    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK) //200
    public EventFullDto updateEvent_1(@PathVariable Long eventId,
                                            @RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest) {

        log.info("/admin/events/{eventId} AdminEventController.updateEvent_1(eventId {}, updateEventAdminRequest {}):", eventId, updateEventAdminRequest);
        return eventService.updateEvent_1(eventId, updateEventAdminRequest);
    }

}