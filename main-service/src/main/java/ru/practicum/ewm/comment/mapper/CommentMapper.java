package ru.practicum.ewm.comment.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {
    public static CommentDto toCommentDto(Comment e) {
        return CommentDto.builder()
                .id(e.getId())
                .content(e.getContent())
                .status(e.getStatus())
                .eventId(e.getEvent() != null ? e.getEvent().getId() : 0L)
                .creatorId(e.getCreator() != null ? e.getCreator().getId() : 0L)
                .created(e.getCreated())
                .build();
    }

    public static NewCommentDto toNewCommentDto(Comment e) {
        return NewCommentDto.builder()
                .content(e.getContent())
                .status(e.getStatus())
                .build();
    }

    public static Comment toComment(CommentDto e, Event event, User creator) {
        return Comment.builder()
                .id(e.getId())
                .content(e.getContent())
                .status(e.getStatus())
                .event(event)
                .creator(creator)
                .created(e.getCreated())
                .build();
    }


    public static Comment toComment(NewCommentDto e) {
        return Comment.builder()
                .content(e.getContent())
                .status(e.getStatus())
                .event(null)
                .creator(null)
                .created(LocalDateTime.now())
                .build();
    }

}
