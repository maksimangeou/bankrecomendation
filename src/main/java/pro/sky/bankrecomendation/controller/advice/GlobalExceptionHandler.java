package pro.sky.bankrecomendation.controller.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pro.sky.bankrecomendation.exception.DynamicRuleNotFoundException;
import pro.sky.bankrecomendation.exception.DynamicRuleValidationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DynamicRuleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleDynamicRuleNotFound(DynamicRuleNotFoundException ex) {
        log.warn("Dynamic rule not found: {}", ex.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", "Dynamic Rule Not Found");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DynamicRuleValidationException.class)
    public ResponseEntity<Map<String, String>> handleDynamicRuleValidation(DynamicRuleValidationException ex) {
        log.warn("Dynamic rule validation failed: {}", ex.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", "Validation Failed");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}