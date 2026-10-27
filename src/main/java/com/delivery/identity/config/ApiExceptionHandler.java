package com.delivery.identity.config;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Devolve mensagens úteis para o frontend (campo {@code message}/{@code detail}),
 * em vez do corpo genérico do Spring sem o motivo da recusa.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException exception) {
        HttpStatusCode status = exception.getStatusCode();
        String message = exception.getReason() == null || exception.getReason().isBlank()
                ? "Não foi possível concluir a solicitação."
                : exception.getReason();
        return ResponseEntity.status(status).body(body(status.value(), message, List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        List<Map<String, String>> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> errors.add(Map.of(
                "field", error.getField(),
                "message", error.getDefaultMessage() == null ? "Valor inválido" : error.getDefaultMessage()
        )));
        String message = errors.isEmpty() ? "Confira os campos informados." : errors.get(0).get("message");
        return ResponseEntity.badRequest().body(body(400, message, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException exception) {
        String message = "Não foi possível ler os dados enviados. Confira o perfil e os campos informados.";
        return ResponseEntity.badRequest().body(body(400, message, List.of()));
    }

    private Map<String, Object> body(int status, String message, List<Map<String, String>> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("detail", message);
        if (!errors.isEmpty()) {
            body.put("errors", errors);
        }
        return body;
    }
}
