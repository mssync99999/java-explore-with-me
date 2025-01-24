package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import java.util.List;

//Публичный API для работы с категориями
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    //Получение категорий
    @GetMapping
    public List<CategoryDto> getCategories(@RequestParam(name = "from", defaultValue = "0") Integer from,
                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("PublicCategoryController.getCategories(from {}, size {})", from, size);
        return categoryService.getCategories(from, size);
    }

    //Получение информации о категории по её идентификатору
    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("PublicCategoryController.getCategory(catId {}):", catId);
        return categoryService.getCategory(catId);
    }

}