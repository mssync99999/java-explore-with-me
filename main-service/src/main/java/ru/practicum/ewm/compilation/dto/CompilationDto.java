package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.EventShortDto;
import java.util.List;

//Подборка событий
@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private List<EventShortDto> events; //Список идентификаторов событий входящих в подборку ?-Set<Long>

    @NotNull
    private Long id; //Идентификатор

    @NotNull
    private Boolean pinned; //Закреплена ли подборка на главной странице сайта

    @NotBlank
    @Size(min = 1,max = 50)
    private String title; //Заголовок подборки
}