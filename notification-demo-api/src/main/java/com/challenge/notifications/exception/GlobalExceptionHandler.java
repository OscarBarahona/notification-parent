package com.challenge.notifications.exception;

import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.NotificationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ApiErrorResponse> handleValidation(
            ValidationException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.BAD_REQUEST, exception, request.getRequestURI());
    }

    @ExceptionHandler(ProviderException.class)
    ResponseEntity<ApiErrorResponse> handleProvider(
            ProviderException exception,
            HttpServletRequest request
    ) {
        log.error("Notification provider failure: {}", exception.getMessage());
        return response(HttpStatus.BAD_GATEWAY, exception, request.getRequestURI());
    }

    @ExceptionHandler(ConfigurationException.class)
    ResponseEntity<ApiErrorResponse> handleConfiguration(
            ConfigurationException exception,
            HttpServletRequest request
    ) {
        log.error("Notification configuration failure: {}", exception.getMessage());
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception, request.getRequestURI());
    }

    @ExceptionHandler(CompletionException.class)
    ResponseEntity<ApiErrorResponse> handleCompletion(
            CompletionException exception,
            HttpServletRequest request
    ) {
        Throwable cause = exception.getCause();
        if (cause instanceof ValidationException validationException) {
            return handleValidation(validationException, request);
        }
        if (cause instanceof ProviderException providerException) {
            return handleProvider(providerException, request);
        }
        if (cause instanceof ConfigurationException configurationException) {
            return handleConfiguration(configurationException, request);
        }
        log.error("Unexpected asynchronous notification failure", cause);
        return internalError(request.getRequestURI());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, String> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null
                                ? "Invalid value"
                                : fieldError.getDefaultMessage(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "REQUEST_VALIDATION_ERROR",
                "Request validation failed",
                requestPath(request),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(body);
    }


    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "MALFORMED_REQUEST",
                "Request body is malformed or contains unsupported values",
                requestPath(request),
                Map.of()
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(NotificationException.class)
    ResponseEntity<ApiErrorResponse> handleNotificationException(
            NotificationException exception,
            HttpServletRequest request
    ) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception, request.getRequestURI());
    }

    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ApiErrorResponse> handleUnexpected(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        log.error("Unexpected notification API failure", exception);
        return internalError(request.getRequestURI());
    }

    private static ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            NotificationException exception,
            String path
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                exception.getErrorCode().name(),
                exception.getMessage(),
                path,
                Map.of()
        ));
    }

    private static ResponseEntity<ApiErrorResponse> internalError(String path) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                path,
                Map.of()
        ));
    }

    private static String requestPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "";
    }
}
