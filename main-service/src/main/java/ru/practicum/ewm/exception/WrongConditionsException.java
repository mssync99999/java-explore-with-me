package ru.practicum.ewm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) //409
public class WrongConditionsException extends RuntimeException {
    public WrongConditionsException(String message) {
        super(message);
    }
}
