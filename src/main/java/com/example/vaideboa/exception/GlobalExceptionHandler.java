package com.example.vaideboa.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.vaideboa.Dtos.ApiResponse;

import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

    String mensagem = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getDefaultMessage())
            .findFirst()
            .orElse("Erro de validação");

    return ResponseEntity
        .badRequest()
        .body(new ApiResponse(false, mensagem));
}
}
