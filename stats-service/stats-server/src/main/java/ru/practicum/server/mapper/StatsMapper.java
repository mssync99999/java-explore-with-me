package ru.practicum.server.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.server.model.Stat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatsMapper {
    public static Stat toStat(EndpointHitDto endpointHitDto) {
        return Stat.builder()
                .app(endpointHitDto.getApp())
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .timestamp(endpointHitDto.getTimestamp())
                .build();

    }

    public static EndpointHitDto toEndpointHitDto(Stat stat) {
        return EndpointHitDto.builder()
                .app(stat.getApp())
                .uri(stat.getUri())
                .ip(stat.getIp())
                .timestamp(stat.getTimestamp())
                .build();
    }

}
