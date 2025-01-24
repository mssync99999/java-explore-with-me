package ru.practicum.ewm.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.service.CompilationService;

//API для работы с подборками событий
@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {

    private final CompilationService compilationService;

    //Добавление новой подборки
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //201
    public CompilationDto saveCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("AdminCompilationController.saveCompilation(newCompilationDto {})", newCompilationDto);
        return compilationService.saveCompilation(newCompilationDto);
    }

    //Удаление подборки
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("AdminCompilationController.deleteCompilation(compId {}):", compId);
        compilationService.deleteCompilation(compId);
    }

    //Изменение подборки
    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK) //200
    public CompilationDto updateCompilation(@PathVariable Long compId,
                                   @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("AdminCompilationController.updateCompilation(compId {}, updateCompilationRequest {}):", compId, updateCompilationRequest);
        return compilationService.updateCompilation(compId, updateCompilationRequest);
    }

}