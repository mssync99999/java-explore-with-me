package ru.practicum.ewm.comment.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.QComment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.enums.SortComment;
import ru.practicum.ewm.enums.StatusComment;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<CommentDto> getComments(Long eventId, String content, StatusComment status, Long creatorId, LocalDateTime rangeStart, LocalDateTime rangeEnd, SortComment sort, Integer from, Integer size, HttpServletRequest request) {
        //пользователям(кроме админа) можно просматривать только PUBLISHED
        //базовые проверки
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("Дата окончания rangeEnd раньше даты начала события rangeStart.");
        }

        //определяем сортировку и пагинацию,
        Sort sortAsc = switch (sort) {
            case SortComment.CREATED_DESC -> Sort.by(Sort.Direction.DESC, "created");
            case SortComment.CREATED_ASC -> Sort.by(Sort.Direction.ASC, "created");
        };
        PageRequest pagenation = PageRequest.of(from / size, size, sortAsc);

        //подготовка и получение данных из репозитория через Query DSL
        BooleanExpression conditions = QComment.comment.event.id.eq(eventId); //инициализируем стартовым условием

        if (content != null && !content.isBlank()) {
            String s = "%" + content.trim() + "%";
            conditions = conditions.and(QComment.comment.content.likeIgnoreCase(s));


        }

        if (status != null) {
            conditions = conditions.and(QComment.comment.status.eq(status));
        }

        if (creatorId != null) {
            conditions = conditions.and(QComment.comment.creator.id.eq(creatorId));
        }

        if (rangeStart != null) {
            conditions = conditions.and(QComment.comment.created.after(rangeStart)); //дата и время не раньше которых должно произойти

        }

        if (rangeEnd != null) {
            conditions = conditions.and(QComment.comment.created.before(rangeEnd)); //дата и время не позже которых должно произойти
        }

        if (rangeStart == null && rangeEnd == null) {
            //если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно
            //выгружать события, которые произойдут позже текущей даты и времени
            conditions = conditions.and(QComment.comment.created.after(LocalDateTime.now()));
        }


        List<Comment> commentsList = commentRepository.findAll(conditions, pagenation).getContent();

        return commentsList.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto getComment(Long commentId, HttpServletRequest request) {
        //пользователям(кроме админа) можно просматривать только PUBLISHED
        Comment comment = commentRepository.findByIdAndStatus(commentId, StatusComment.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        return CommentMapper.toCommentDto(comment);
    }

    @Transactional
    @Override
    public CommentDto createComment(Long creatorId, Long eventId, NewCommentDto newCommentDto) {
        //+пользователю нельзя создавать новые комментарий на событии,
        //+если у пользователя уже есть под этим событием хотя бы 1 коммент с банном

        //базовые проверки
        if (commentRepository.existsByContent(newCommentDto.getContent())) {
            throw new DoubleException("Ошибка. Comment уже существует");
        }

        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("User with id=" + creatorId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        //--пользователю нельзя создават новые комментарий на событии,
        //если у пользователя уже есть под этим событием хотя бы 1 коммент с банном
        if (commentRepository.existsByCreatorAndEventAndStatus(user, event, StatusComment.BANNED)) {
            throw new WrongUserException("Автор забанен в этом событии");
        }

        Comment newObj = CommentMapper.toComment(newCommentDto);
        newObj.setCreator(user);
        newObj.setEvent(event);

        return CommentMapper.toCommentDto(commentRepository.save(newObj));
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long creatorId, Long commentId, UpdateCommentDto obj) {


        //базовые проверки
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        User user = null;
        if (!creatorId.equals(-1L)) {
            user = userRepository.findById(creatorId)
                    .orElseThrow(() -> new NotFoundException("User with id=" + creatorId + " was not found"));

            //--пользователю нельзя изменять свои комментарий на событии,
            //если у пользователя уже есть под этим событием хотя бы 1 коммент с банном
            if (commentRepository.existsByCreatorAndEventAndStatus(user, comment.getEvent(), StatusComment.BANNED)) {
                throw new WrongUserException("Автор забанен в этом событии");
            }

            if (!comment.getCreator().equals(user)) {
                throw new WrongUserException("Редактировать комментарий может только автор и администратор");
            }
        }


        //1.2 автор отредактировал свой старый коммент UPDATING/PENDING -> PENDING (коммент ожидает ревью админа И коммент НЕ доступен пользователям при поиске) (Ожидается код ошибки 409)
        if (obj.getStatus() == StatusComment.PENDING && !creatorId.equals(-1L)) {
            if (comment.getStatus() != StatusComment.UPDATING && comment.getStatus() != obj.getStatus()) {
                throw new WrongConditionsException("Нарушена логика изменения статуса комментария");
            }
        }

        //Автор не может публиковать свои комментарии
        if (obj.getStatus() == StatusComment.PUBLISHED && !creatorId.equals(-1L)) {
            throw new WrongConditionsException("Автор не может публиковать свои комментарии");
        }

        //2.1 админ согласовал коммент PENDING -> PUBLISHED (автор приобретает полномочия изменять статус комменту И коммент доступен пользователям при поиске)
        //--4.1 админ разбанил коммент BANNED -> PUBLISHED (автор приобретает полномочия изменять статус комменту И коммент доступен пользователям при поиске)
        if (obj.getStatus() == StatusComment.PUBLISHED && creatorId.equals(-1L)) {
            if (comment.getStatus() != StatusComment.PENDING && comment.getStatus() != StatusComment.BANNED && comment.getStatus() != obj.getStatus()) {
                throw new WrongConditionsException("Нарушена логика изменения статуса комментария");
            }
        }

        //--2.2 админ забанил коммент PENDING -> BANNED (тогда автор теряет возможность комментировать это событие И коммент НЕ доступен пользователям при поиске)
        //--4.1 админ забанил опубликованный коммент PUBLISHED -> BANNED (тогда автор теряет возможность комментировать это событие И коммент НЕ доступен пользователям при поиске)
        if (obj.getStatus() == StatusComment.BANNED && creatorId.equals(-1L)) {
            if (comment.getStatus() != StatusComment.PENDING && comment.getStatus() != StatusComment.PUBLISHED && comment.getStatus() != obj.getStatus()) {
                throw new WrongConditionsException("Нарушена логика изменения статуса комментария");
            }
        }

        //--3.1 автор отменил свой коммент (без удаления из БД) PUBLISHED/UPDATING -> CANCELED (автор теряет полномочия изменять статус комменту)
        if (obj.getStatus() == StatusComment.CANCELED && !creatorId.equals(-1L)) {
            if (comment.getStatus() != StatusComment.UPDATING && comment.getStatus() != StatusComment.PUBLISHED && comment.getStatus() != obj.getStatus()) {
                throw new WrongConditionsException("Нарушена логика изменения статуса комментария");
            }
        }

        //--2.3 админ вернул коммент на редактирование автору PENDING -> UPDATING (коммент НЕ доступен пользователям при поиске)
        if (obj.getStatus() == StatusComment.UPDATING && creatorId.equals(-1L)) {
            if (comment.getStatus() != StatusComment.PENDING && comment.getStatus() != obj.getStatus()) {
                throw new WrongConditionsException("Нарушена логика изменения статуса комментария");
            }
        }

        //3.2 автор взял на редактирование свой коммент PUBLISHED -> UPDATING (коммент НЕ доступен пользователям при поиске)
        if (obj.getStatus() == StatusComment.UPDATING && !creatorId.equals(-1L)) {
            if (comment.getStatus() != StatusComment.PUBLISHED && comment.getStatus() != obj.getStatus()) {
                throw new WrongConditionsException("Нарушена логика изменения статуса комментария");
            }
        }

        //обновление изменяемых полей
        if (obj.getContent() != null && !obj.getContent().isBlank()) {
            comment.setContent(obj.getContent());
        }
        if (obj.getStatus() != null) {
            comment.setStatus(obj.getStatus());
        }

        return CommentMapper.toCommentDto(commentRepository.save(comment));

    }

    @Transactional
    @Override
    public void deleteComment(Long creatorId, Long commentId) {
        //+пользователю нельзя удалять свои комментарий на событии,
        //+если у пользователя уже есть под этим событием хотя бы 1 коммент с банном

        //базовые проверки
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));

        User user = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("User with id=" + creatorId + " was not found"));


        if (!comment.getCreator().equals(user) && !creatorId.equals(0L)) {
            throw new WrongUserException("Удалять комментарий может только автор или админ");
        }

        //--пользователю нельзя удалять свои комментарий на событии,
        //если у пользователя уже есть под этим событием хотя бы 1 коммент с банном
        if (commentRepository.existsByCreatorAndEventAndStatus(user, comment.getEvent(), StatusComment.BANNED)) {
            throw new WrongUserException("Автор забанен в этом событии");
        }

        commentRepository.deleteById(commentId);

    }


}