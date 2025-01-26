package ru.practicum.ewm.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import java.time.LocalDateTime;
import ru.practicum.ewm.event.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    public static EventShortDto toEventShortDto(Event e) {
        return EventShortDto.builder()
                .id(e.getId())
                .annotation(e.getAnnotation())
                .category(CategoryMapper.toCategoryDto(e.getCategory()))
                .confirmedRequests(e.getConfirmedRequests())
                .eventDate(e.getEventDate())
                .initiator(UserMapper.toUserShortDto(e.getInitiator()))
                .paid(e.getPaid())
                .title(e.getTitle())
                .views(e.getViews())
                .build();
    }

    public static EventFullDto toEventFullDto(Event e) {
        return EventFullDto.builder()
                .id(e.getId())
                .annotation(e.getAnnotation())
                .category(CategoryMapper.toCategoryDto(e.getCategory()))
                .confirmedRequests(e.getConfirmedRequests())
                .createdOn(e.getCreatedOn())
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .initiator(UserMapper.toUserShortDto(e.getInitiator()))
                .location(e.getLocation())
                .paid(e.getPaid())
                .participantLimit(e.getParticipantLimit())
                .publishedOn(e.getPublishedOn())
                .requestModeration(e.getRequestModeration())
                .state(e.getState())
                .title(e.getTitle())
                .views(e.getViews())
                .build();
    }

    public static Event toEvent(NewEventDto e, Category category, User initiator,Location location) {
        return Event.builder()
                .annotation(e.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now()) //необходимая константа при создании нового объекта
                .confirmedRequests(0L) //необходимая константа при создании нового объекта
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .initiator(initiator)
                .location(location)
                .paid(e.getPaid() != null ? e.getPaid() : false)
                .participantLimit(e.getParticipantLimit() != null ? e.getParticipantLimit() : 0)
                .publishedOn(LocalDateTime.now()) //необходимая константа при создании нового объекта
                .requestModeration(e.getRequestModeration() != null ? e.getRequestModeration() : true)
                .state(State.PENDING) //необходимая константа при создании нового объекта
                .title(e.getTitle())
                .views(0L) //необходимая константа при создании нового объекта
                .build();
    }

}
