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
@RestControllerAdvice //включает централизованную(глобальную) обработку ошибок всего проекта
public class ErrorHandler {


    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final ConstraintViolationException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler //? или @ExceptionHandler(DoubleException.class)
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final DoubleException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler //? или @ExceptionHandler(BadInitiatorException.class)
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

    @ExceptionHandler //? или @ExceptionHandler(BadLimitException.class)
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final BadLimitException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler //? или @ExceptionHandler(WrongEventDateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final WrongEventDateException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Нарушение целостности данных", HttpStatus.CONFLICT);
    }

    @ExceptionHandler //? или @ExceptionHandler(WrongConditionsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) //409
    public ApiError handle(final WrongConditionsException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Событие не удовлетворяет правилам редактирования", HttpStatus.CONFLICT);
    }

    @ExceptionHandler //? или @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public ApiError handle(final BadRequestException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Запрос составлен некорректно", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler //? или @ExceptionHandler(BadStatServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //500
    public ApiError handle(final BadStatServiceException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Запрос составлен некорректно", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //
    /*
    //MethodArgumentNotValidException вернёт 400 для @Validated в методе контроллера
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    public ApiError handle(final MethodArgumentNotValidException e) {
        log.debug("Получено исключение {}", e.getMessage());
        return new ApiError(e, e.getMessage(), "Запрос составлен некорректно", HttpStatus.BAD_REQUEST);
    }

     */

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

        /*
                if (ex instanceof MissingRequestHeaderException) {
            MissingRequestHeaderException headerEx = (MissingRequestHeaderException) ex;
            error.put("error", "Отсутствует обязательный заголовок: " + headerEx.getHeaderName());
        } else if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException paramEx = (MissingServletRequestParameterException) ex;
            error.put("error", "Отсутствует обязательный параметр: " + paramEx.getParameterName());
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException typeMismatchEx = (MethodArgumentTypeMismatchException) ex;
            error.put("error", "Неверный тип аргумента: " + typeMismatchEx.getName());
        } else if (ex instanceof HttpMessageNotReadableException) {
            HttpMessageNotReadableException notReadableEx = (HttpMessageNotReadableException) ex;
            error.put("error", "Ошибка чтения тела запроса: " + notReadableEx.getMessage());
        }
         */
        return new ApiError(e, e.getMessage(), "Запрос составлен некорректно", HttpStatus.BAD_REQUEST);
    }




    /*
    @ExceptionHandler //перенаправляет исключение TestMyAnyCustomException в этот метод
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTestMyAnyCustomException(final TestMyAnyCustomException e) {
        //TestMyAnyCustomException - моё кастомное исключение вызываемое где-либо через: throw new ...
        log.debug("Получен статус 400 BAD_REQUEST {}", e);

        //ConstraintViolationException 409


        return new ApiError("Ошибка ...", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) //500
    public ErrorResponse handle(final Exception e) {
        return new ErrorResponse("Ошибка ...", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeException(final RuntimeException e) {
        return new ErrorResponse("Ошибка ...", e.getMessage());
    }

     */


}

/*
//если требуется обработать ошибки только в контроллере CatsInteractionController
@ControllerAdvice(assignableTypes = CatsInteractionController.class)

ИЛИ

//если требуется обработать ошибки в некоторых контроллерах
@ControllerAdvice(assignableTypes = {DogsInteractionController.class, CatsInteractionController.class})

//Если необходимо обработать сразу все классы, которые находятся в одном пакете, можно воспользоваться одним из следующих вариантов:

@ControllerAdvice("ru.yandex.practicum.controller")
или
@ControllerAdvice(value = "ru.yandex.practicum.controller")
или
@ControllerAdvice(basePackages = "ru.yandex.practicum.controller")
 */
