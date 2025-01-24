package ru.practicum.ewm.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

//API для работы с категориями
@Slf4j
@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    //Добавление новой категории
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //201
    public CategoryDto addCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        log.info("AdminCategoryController.addCategory(newCategoryDto {})", newCategoryDto);
        return categoryService.addCategory(newCategoryDto);
    }

    //Удаление категории
    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteCategory(@PathVariable Long catId) {
        log.info("AdminCategoryController.deleteCategory(catId {}):", catId);
        categoryService.deleteCategory(catId);
    }

    //Изменение категории
    @PatchMapping("/{catId}")
    @ResponseStatus(HttpStatus.OK) //200
    public CategoryDto updateCategory(@PathVariable Long catId,
                                   @RequestBody @Valid CategoryDto categoryDto) {
        log.info("AdminCategoryController.updateCategory(catId {}, categoryDto {}):", catId, categoryDto);
        return categoryService.updateCategory(catId, categoryDto);
    }

}