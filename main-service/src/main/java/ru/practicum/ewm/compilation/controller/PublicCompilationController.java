package ru.practicum.ewm.compilation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.service.CompilationService;
import java.util.List;

//Публичный API для работы с подборками событий
@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    //Получение подборок
    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(name = "pinned", defaultValue = "0", required = false) Boolean pinned,
                                                @RequestParam(name = "from", defaultValue = "0", required = false) Integer from,
                                                @RequestParam(name = "size", defaultValue = "10", required = false) Integer size) {
        log.info("PublicCompilationController.getCompilatiosn(pinned {}, from {}, size {})", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    //Получение подборки событий по его id
    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("PublicCompilationController.getCompilation(compId {}):", compId);
        return compilationService.getCompilation(compId);
    }

}
