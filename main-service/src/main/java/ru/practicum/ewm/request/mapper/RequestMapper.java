package ru.practicum.ewm.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.dto.UserShortDto;
import ru.practicum.ewm.user.model.User;

//@UtilityClass
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(Request e) {
        return ParticipationRequestDto.builder()
                .id(e.getId())

                .event(e.getEvent().getId())
                .requester(e.getRequester().getId())
                .created(e.getCreated())
                .status(e.getStatus())
                //...
                //.itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .build();
    }

    /*не используется
    public static Request toRequest(ParticipationRequestDto e) {
        return Request.builder()
                .id(e.getId())
                //.event(e.getEvent().getId())
                //.requester(e.getRequester().getId())
                .created(e.getCreated())
                .status(e.getStatus())
                //...
                //.itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .build();
    }

     */

    /*
        public List<ParticipationRequestDto> toParticipationRequestDto(List<ParticipationRequest> requests) {
        return requests.stream().map(ParticipationRequestMapper::toParticipationRequestDto).toList();
    }
     */


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