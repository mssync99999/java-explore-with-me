package ru.practicum.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsServiceImpl;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public void create(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("StatsController.create(endpointHitDto {}):", endpointHitDto);

        statsServiceImpl.create(endpointHitDto);

    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("StatsController.getStats(start {}, end {}, uris {}, unique {}):",
                start, end, uris, unique);
        return statsServiceImpl.getStats(start, end, uris, unique);
    }


}
