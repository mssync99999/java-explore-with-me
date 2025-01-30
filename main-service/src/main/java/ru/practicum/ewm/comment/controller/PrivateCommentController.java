package ru.practicum.ewm.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

//Закрытый API для работы с комментариями
@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PrivateCommentController {

    private final CommentService commentService;


    //Добавление нового комментария
    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED) //201
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") @Positive Long creatorId,
                                      @PathVariable Long eventId,
                                      @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("/comments/events/{eventId} PrivateCommentController.createComment(creatorId {}, eventId {}, newCommentDto {})", creatorId, eventId, newCommentDto);
        return commentService.createComment(creatorId, eventId, newCommentDto);
    }


    //Изменение комментария (проверка авторства)
    @PatchMapping("{commentId}")
    @ResponseStatus(HttpStatus.OK) //200
    public CommentDto updateComment(@RequestHeader("X-Sharer-User-Id") @Positive Long creatorId,
                                    @PathVariable Long commentId,
                                    @RequestBody @Valid UpdateCommentDto updateCommentDto) {
        log.info("/comments/{commentId} PrivateCommentController.updateComment(creatorId {}, commentId {}, updateCommentDto {}):", creatorId, commentId, updateCommentDto);
        return commentService.updateComment(creatorId, commentId, updateCommentDto);
    }


    //Удаление комментария (проверка авторства)
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteComment(@RequestHeader("X-Sharer-User-Id") @Positive Long creatorId, @PathVariable Long commentId) {
        log.info("/comments/{commentId} PrivateCommentController.deleteCompilation(compId {}):", commentId);
        commentService.deleteComment(creatorId, commentId);
    }

}
