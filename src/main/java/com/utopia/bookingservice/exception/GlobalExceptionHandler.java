package com.utopia.bookingservice.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.Getter;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Getter
    static class ApiError {
        private HttpStatus status;
        private String message;
        private List<String> errors;

        public ApiError(HttpStatus status, String message,
                List<String> errors) {
            super();
            this.status = status;
            this.message = message;
            this.errors = errors;
        }

        public ApiError(HttpStatus status, String message, String error) {
            super();
            this.status = status;
            this.message = message;
            errors = Arrays.asList(error);
        }

        public ApiError(HttpStatus status, String message) {
            super();
            this.status = status;
            this.message = message;
            errors = Arrays.asList();
        }
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex, final HttpHeaders headers,
            final HttpStatus status, final WebRequest request) {
        logger.info(ex.getClass().getName());
        final List<String> errors = new ArrayList<String>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult()
                .getGlobalErrors()) {
            errors.add(
                    error.getObjectName() + ": " + error.getDefaultMessage());
        }
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,
                ex.getLocalizedMessage(), errors);
        return handleExceptionInternal(ex, apiError, headers,
                apiError.getStatus(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public void handleResponseStatusException(
            ResponseStatusException exception) {
        throw exception;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgumentException(
            IllegalArgumentException exception) {
        throw new ModelMapperFailedException(exception);
    }

    @ExceptionHandler(ConfigurationException.class)
    public void handleConfigurationException(ConfigurationException exception) {
        throw new ModelMapperFailedException(exception);
    }

    @ExceptionHandler(MappingException.class)
    public void handleMappingException(MappingException exception) {
        throw new ModelMapperFailedException(exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(
            AccessDeniedException exception) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUncaughtException(Exception exception) {
        System.out.printf("An unknown error occurred.", exception);
        exception.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }
}
