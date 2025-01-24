package ru.practicum.ewm.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.enums.State;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
//import ru.practicum.ewm.event.dto.TestDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
//import ru.practicum.ewm.event.model.Test;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ru.practicum.ewm.event.model.Location;

//@UtilityClass
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {
    public static EventShortDto toEventShortDto(Event e) {
        return EventShortDto.builder()
                .id(e.getId())
                .annotation(e.getAnnotation())
                .category(CategoryMapper.toCategoryDto(e.getCategory())) //CategoryMapper.toCategoryDto(e.getCategory())
                .confirmedRequests(e.getConfirmedRequests())
                //.createdOn(e.getCreatedOn())
                //.description(e.getDescription())
                .eventDate(e.getEventDate())
                .initiator(UserMapper.toUserShortDto(e.getInitiator())) //UserMapper.toUserShortDto(e.getInitiator())
                //.location(e.getLocation())
                .paid(e.getPaid())
                //.participantLimit(e.getParticipantLimit())
                //.publishedOn(e.getPublishedOn())
                //.requestModeration(e.getRequestModeration())
                //.state(e.getState())
                .title(e.getTitle())
                .views(e.getViews())
                //...
                //.itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .build();
    }

    public static EventFullDto toEventFullDto(Event e) {
        return EventFullDto.builder()
                .id(e.getId())
                .annotation(e.getAnnotation())
                .category(CategoryMapper.toCategoryDto(e.getCategory())) //CategoryMapper.toCategoryDto(e.getCategory())
                .confirmedRequests(e.getConfirmedRequests())
                .createdOn(e.getCreatedOn())
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .initiator(UserMapper.toUserShortDto(e.getInitiator())) //UserMapper.toUserShortDto(e.getInitiator())
                .location(e.getLocation()) //? -.location(LocationMapper.toLocationDto(e.getLocation()))
                .paid(e.getPaid())
                .participantLimit(e.getParticipantLimit())
                .publishedOn(e.getPublishedOn())
                .requestModeration(e.getRequestModeration())
                .state(e.getState())
                .title(e.getTitle())
                .views(e.getViews())
                //...
                //.itemId(booking.getItem() != null ? booking.getItem().getId() : null)
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
                //.paid(e.getPaid())
                .participantLimit(e.getParticipantLimit() != null ? e.getParticipantLimit() : 0)
                //.participantLimit(e.getParticipantLimit())
                .publishedOn(LocalDateTime.now()) //необходимая константа при создании нового объекта
                .requestModeration(e.getRequestModeration() != null ? e.getRequestModeration() : true)
                //.requestModeration(e.getRequestModeration())
                .state(State.PENDING) //необходимая константа при создании нового объекта
                .title(e.getTitle())
                .views(0L) //необходимая константа при создании нового объекта
                //.participantLimit(e.getParticipantLimit() != null ? e.getParticipantLimit() : 0)
                //.requestModeration(e.getRequestModeration() != null ? e.getRequestModeration() : true)
                .build();
    }



}

/*
        String regDate = DateTimeFormatter
                .ofPattern("yyyy.MM.dd hh:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(user.getRegistrationDate());
 */

/*ИНОЙ ПОДХОД - ПРИМЕРЫ

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Component
@Mapper(componentModel = SPRING)
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toCategory(CategoryRequestDto dto);

    CategoryDto toDto(Category category);

    List<CategoryDto> toDtoList(Page<Category> categories);
}

---

import org.mapstruct.Mapper;

@Mapper
public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(),
                category.getName());
    }
}

---

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface CategoryMapper {
    Category toCategory(CategoryDto categoryDto);

    CategoryDto toCategoryDto(Category category);
}
---
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {DirectorMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface FilmMapper {

    FilmDto toFilmDto(Film film);

    Film toFilm(FilmDto filmDto);
}
 */