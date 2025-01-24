package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

//Подборка событий
@Data //Lombok, чтобы сгенерировать геттеры и сеттеры для полей
@Builder //создаёт через билдер произвольный конструктор
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {
    private List<Long> events; //Список идентификаторов событий входящих в подборку

    @Builder.Default
    private Boolean pinned = false; //Закреплена ли подборка на главной странице сайта

    @NotBlank
    @Size(min = 1,max = 50)
    private String title; //Заголовок подборки
}
