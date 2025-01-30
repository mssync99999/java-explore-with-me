package ru.practicum.ewm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)//403
public class WrongUserException extends RuntimeException {
    public WrongUserException(String message) {
        super(message);
    }
}
