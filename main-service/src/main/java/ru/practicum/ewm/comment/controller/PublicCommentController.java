package ru.practicum.ewm.comment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.service.CommentService;
import ru.practicum.ewm.enums.SortComment;
import ru.practicum.ewm.enums.StatusComment;
import java.time.LocalDateTime;
import java.util.List;


//Публичный API для работы с комментариями
@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PublicCommentController {

    private final CommentService commentService;

    //Получение комментариев с возможностью фильтрации - ограниченная версия публичного поиска (мало фильтров)
    @GetMapping("")
    public List<CommentDto> getComments(@RequestParam @Positive Long eventId,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                        @RequestParam(name = "size", defaultValue = "10") @Positive Integer size,
                                        HttpServletRequest request) {
        log.info("/comments?...=...& PublicCommentController.getComments(eventId {}, content {}, status {}, creatorId {}, rangeStart {}, rangeEnd {}, sort {}, from {}, size {}, request {})", eventId, "", StatusComment.PUBLISHED, null, rangeStart, rangeEnd, SortComment.CREATED_ASC, from, size, request);

        return commentService.getComments(eventId, "", StatusComment.PUBLISHED, null, rangeStart, rangeEnd, SortComment.CREATED_ASC, from, size, request);
    }

    //Получение комментария по его id
    @GetMapping("/{commentId}")
    public CommentDto getComment(@PathVariable Long commentId, HttpServletRequest request) {
        log.info("/comments/{commentId} PublicCommentController.getComment(commentId {}):", commentId);
        return commentService.getComment(commentId, request);
    }

}