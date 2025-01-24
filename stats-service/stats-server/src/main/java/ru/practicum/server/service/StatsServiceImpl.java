package ru.practicum.server.service;

//import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import ru.practicum.server.exception.BadRequestException;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.repository.StatsRepository;
import ru.practicum.server.mapper.StatsMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public void create(EndpointHitDto endpointHitDto) {
        statsRepository.save(StatsMapper.toStat(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        if (start.isAfter(end)) {
            throw new BadRequestException("Ошибка: end раньше start");
        }

        if (uris == null || uris.isEmpty()) { //uri - пусто
            if (unique) { // unique - true
                return statsRepository.getUrisAllUniqTrue(start, end);
            } else { // unique - false
                return statsRepository.getUrisAllUniqFalse(start, end);
            }
        } else { //uri - есть
            if (unique) { // unique - true
                return statsRepository.getUrisByUniqTrue(start, end, uris);
            } else { // unique - false
                return statsRepository.getUrisByUniqFalse(start, end, uris);
            }
        }

    }
}
