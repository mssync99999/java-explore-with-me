package ru.practicum.ewm.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.enums.SortType;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.service.EventService;
import java.time.LocalDateTime;
import java.util.List;


//Публичный API для работы с событиями
@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;

    //Получение событий с возможностью фильтрации
    @GetMapping
    public List<EventShortDto> getEvents_1(@RequestParam(name = "text", defaultValue = "") String text,
                                           @RequestParam(name = "categories", required = false) List<Long> categories,
                                           @RequestParam(name = "paid", required = false) Boolean paid,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                           @RequestParam(name = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
                                           @RequestParam(name = "sort", defaultValue = "EVENT_DATE") SortType sort,
                                           @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                           @RequestParam(name = "size", defaultValue = "10") @Positive Integer size,
                                           HttpServletRequest request ) {
        log.info("PublicEventController.getEvents_1(text {}, categories {}, paid {}, rangeStart {}, rangeEnd {}, onlyAvailable {}, sort {}, from {}, size {})", text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return eventService.getEvents_1(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    //Получение подборки событий по его id
    @GetMapping("/{id}")
    public EventFullDto getEvent_1(@PathVariable Long id, HttpServletRequest request) {
        log.info("PublicEventController.getEvent_1(id {}):", id);
        return eventService.getEvent_1(id, request);
    }

}