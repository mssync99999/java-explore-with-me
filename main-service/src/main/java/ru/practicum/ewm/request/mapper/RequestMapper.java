package ru.practicum.ewm.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.Request;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(Request e) {
        return ParticipationRequestDto.builder()
                .id(e.getId())
                .event(e.getEvent().getId())
                .requester(e.getRequester().getId())
                .created(e.getCreated())
                .status(e.getStatus())
                .build();
    }

}