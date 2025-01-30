package ru.practicum.ewm.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.enums.StatusComment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment> {

    Optional<Comment> findByIdAndStatus(Long commentId, StatusComment status);

    Boolean existsByContent(String content);

    Boolean existsByCreatorAndEventAndStatus(User creator, Event event, StatusComment status);

}
