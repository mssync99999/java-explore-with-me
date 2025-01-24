package ru.practicum.ewm.exception;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data // геттеры необходимы, чтобы Spring Boot мог получить значения полей
public class ApiError {
    @JsonIgnore
    private List<StackTraceElement> error;

    private String message;
    private String reason;
    private String status;
    private String timestamp; //private LocalDateTime timestamp;

    public ApiError(Exception e, String message, String reason, HttpStatus status) {
        this.message = message;
        this.reason = reason;
        this.status = status.getReasonPhrase().toUpperCase();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
