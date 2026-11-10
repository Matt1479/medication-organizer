package com.matt1479.medicationorganizer.exceptions;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.matt1479.medicationorganizer.exceptions.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(GlobalExceptionHandler.class);

    /* Handles invalid method parameters */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        String message = exception.getConstraintViolations()
                .stream()
                .map((violation) -> violation.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return createErrorResponse(
                status,
                message.isBlank()
                        ? "One or more parameters are invalid"
                        : message,
                request.getRequestURI());
    }

    /*
     * Handles validation failures on controller method parameters,
     * such as invalid @PathVariable and @RequestParam values.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidationException(
            HandlerMethodValidationException exception,
            HttpServletRequest request) {

        String message = exception.getParameterValidationResults()
                .stream()
                .flatMap((result) -> result.getResolvableErrors()
                        .stream()
                        .map((error) -> error.getDefaultMessage()))
                .filter((error) -> Objects.nonNull(error))
                .distinct()
                .collect(Collectors.joining("; "));

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message.isBlank()
                        ? "One or more parameters are invalid"
                        : message,
                request.getRequestURI());
    }

    /*
     * Handles invalid request bodies, such as missing required fields
     * or invalid email addresses.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message.isBlank()
                        ? "Validation failed"
                        : message,
                request.getRequestURI());
    }

    /*
     * Handles malformed or unreadable request bodies.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid or malformed request body",
                request.getRequestURI());
    }

    /*
     * Handles requests made with unsupported HTTP methods.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method not supported for this endpoint",
                request.getRequestURI());
    }

    /*
     * Handles failed login attempts caused by invalid credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException exception,
            HttpServletRequest request) {

        logger.warn("Authentication failed for request: {}", request.getRequestURI());

        return createErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password",
                request.getRequestURI());
    }

    /*
     * Handles authorization exceptions that reach Spring MVC's
     * exception-handling mechanism.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            HttpServletRequest request) {

        logger.warn("Access denied for request: {}", request.getRequestURI());

        return createErrorResponse(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action",
                request.getRequestURI());
    }

    /* Handles requests for resources that do not exist. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            HttpServletRequest request) {

        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                "The requested resource was not found",
                request.getRequestURI());
    }

    /* Preserves the status codes and messages defined in services. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());

        return createErrorResponse(
                status,
                exception.getReason() == null
                        ? status.getReasonPhrase()
                        : exception.getReason(),
                request.getRequestURI());
    }

    /* Handles generic exceptions. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {

        logger.error("Unexpected exception while handling request", exception);

        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(
            HttpStatus status,
            String message,
            String path) {

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path);

        return ResponseEntity.status(status).body(errorResponse);
    }

}
