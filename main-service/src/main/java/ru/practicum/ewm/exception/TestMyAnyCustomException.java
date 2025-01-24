package ru.practicum.ewm.exception;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//400 @ResponseStatus(HttpStatus.BAD_REQUEST)
//404 @ResponseStatus(HttpStatus.NOT_FOUND)
//409 @ResponseStatus(HttpStatus.CONFLICT)
//500 @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//403 @ResponseStatus(HttpStatus.FORBIDDEN)

//моё кастомное исключение вызываемое где-либо через: throw new ...
@ResponseStatus(HttpStatus.FORBIDDEN) //403
public class TestMyAnyCustomException extends RuntimeException {
    public TestMyAnyCustomException(String message) {
        super(message);
    }
}
