package ru.practicum.ewm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) //409
public class WrongEventDateException extends RuntimeException {
    public WrongEventDateException(String message) {
        super(message);
    }
}
