package ru.practicum.ewm.comment.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.enums.SortComment;
import ru.practicum.ewm.enums.StatusComment;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {

    //Получение информации о комментариях
    List<CommentDto> getComments(Long eventId, String content,
                                 StatusComment status, Long creatorId,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                 SortComment sort, Integer from, Integer size,
                                 HttpServletRequest request);

    //Получение информации о комментарии
    CommentDto getComment(Long commentId, HttpServletRequest request);

    //Добавление нового комментария
    CommentDto createComment(Long creatorId, Long eventId, NewCommentDto newCommentDto);

    //Изменение комментария
    CommentDto updateComment(Long creatorId, Long commentId, UpdateCommentDto updateCommentDto);

    //Удаление комментария
    void deleteComment(Long creatorId, Long commentId);

}
