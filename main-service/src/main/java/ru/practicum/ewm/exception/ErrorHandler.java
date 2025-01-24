package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final ConstraintViolationException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final DoubleException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final BadInitiatorException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler //? или @ExceptionHandler(BadStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final BadStateException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final BadLimitException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final WrongEventDateException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final WrongConditionsException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Событие не удовлетворяет правилам редактирования", HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public ApiError handle(final BadRequestException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Запрос составлен некорректно", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //500
    public ApiError handle(final BadStatServiceException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Запрос составлен некорректно", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND) //404
    public ApiError handle(final NotFoundException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "The required object was not found.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public ApiError handle(final Exception e) {
        log.debug("Получено исключение {}", e.getMessage());

        return new ApiError(e, e.getMessage(), "Запрос составлен некорректно", HttpStatus.BAD_REQUEST);
    }

}
