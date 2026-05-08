package com.jobtracker.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final List<String> messages;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.messages = List.of(message);
    }

    public ErrorResponse(int status, String error, List<String> messages) {
        this.status = status;
        this.error = error;
        this.messages = messages;
    }
}
