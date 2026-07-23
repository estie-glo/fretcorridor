package com.flysoft.fretcorridor.common.config;

import com.flysoft.fretcorridor.common.dto.AuthDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthDto.ErreurResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() != null
                        ? error.getDefaultMessage()
                        : error.getField() + " invalide")
                .findFirst()
                .orElse("Données invalides");

        return ResponseEntity.badRequest().body(
                AuthDto.ErreurResponse.builder()
                        .code("VALIDATION_ECHOUEE")
                        .message(message)
                        .build()
        );
    }
}
