package com.kpi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleOthers(Throwable throwable) {
        return ResponseEntity.badRequest().body(throwable.getMessage());
    }
}
