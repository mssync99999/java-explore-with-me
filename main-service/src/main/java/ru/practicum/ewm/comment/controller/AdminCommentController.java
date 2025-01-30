package ru.practicum.ewm.comment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.enums.SortComment;
import ru.practicum.ewm.enums.StatusComment;
import java.time.LocalDateTime;
import java.util.List;

//API для работы с событиями
@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    //Поиск событий - расширенная версия (дополнительные фильтры)
    @GetMapping("")
    public List<CommentDto> getCommentsAdmin(@RequestParam @Positive Long eventId,
                                        @RequestParam(name = "content", defaultValue = "") String content,
                                        @RequestParam(name = "status", defaultValue = "PUBLISHED") StatusComment status,
                                        @RequestParam(name = "creatorId", required = false) Long creatorId,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(name = "sort", defaultValue = "CREATED_ASC") SortComment sort,
                                        @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(name = "size", defaultValue = "10") @Positive Integer size,
                                        HttpServletRequest request) {
        log.info("/comments?...=...& PublicCommentController.getComments(eventId {}, content {}, status {}, creatorId {}, rangeStart {}, rangeEnd {}, sort {}, from {}, size {}, request {})", eventId, content, status, creatorId, rangeStart, rangeEnd, sort, from, size, request);

        return commentService.getComments(eventId, content, status, creatorId, rangeStart, rangeEnd, sort, from, size, request);
    }


    //Изменение комментария - расширенная версия (без проверки авторства)
    @PatchMapping("{commentId}")
    @ResponseStatus(HttpStatus.OK) //200
    public CommentDto updateCommentAdmin(@PathVariable Long commentId,
                                    @RequestBody @Valid UpdateCommentDto updateCommentDto) {
        log.info("/comments/{commentId} PrivateCommentController.updateComment(creatorId {}, commentId {}, updateCommentDto {}):", -1L, commentId, updateCommentDto);
        return commentService.updateComment(-1L, commentId, updateCommentDto);
    }

    //Удаление комментария - расширенная версия (без проверки авторства)
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteCommentAdmin(@PathVariable Long commentId) {
        log.info("/comments/{commentId} PrivateCommentController.deleteCompilation(compId {}):", commentId);
        commentService.deleteComment(-1L, commentId);
    }

}