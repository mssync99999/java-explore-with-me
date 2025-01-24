package ru.practicum.ewm.category.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
//import ru.practicum.ewm.category.dto.TestDto;
import ru.practicum.ewm.category.model.Category;
//import ru.practicum.ewm.category.model.Test;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMapper {
    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                //...
                //.itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .build();
    }

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                //.id(newCategoryDto.getId())
                .name(newCategoryDto.getName())
                //...
                .build();
    }

    public static Category toCategory(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                //...
                .build();
    }
}
