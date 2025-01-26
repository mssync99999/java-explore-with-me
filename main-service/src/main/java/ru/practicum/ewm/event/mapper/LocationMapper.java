package ru.practicum.ewm.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {
    public static LocationDto toLocationDto(Location e) {
        return LocationDto.builder()
                .lat(e.getLat())
                .lon(e.getLon())
                .build();
    }

    public static Location toLocation(LocationDto e) {
        return Location.builder()
                .lat(e.getLat())
                .lon(e.getLon())
                .build();
    }


}
