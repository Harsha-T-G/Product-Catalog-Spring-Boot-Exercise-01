package com.codewalnut.productcatalog.exception;

import com.codewalnut.productcatalog.dto.ErrorResponse;
import com.codewalnut.productcatalog.dto.FieldErrorDetail;
import com.codewalnut.productcatalog.security.SafeLogValue;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSku(
            DuplicateSkuException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(AppUserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAppUserNotFound(
            AppUserNotFoundException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler({DuplicateUsernameException.class, SelfDisableNotAllowedException.class})
    public ResponseEntity<ErrorResponse> handleUserConflict(
            RuntimeException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(ProductLimitReachedException.class)
    public ResponseEntity<ErrorResponse> handleProductLimitReached(
            ProductLimitReachedException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "A database constraint was violated",
                request,
                null);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Product was updated by another transaction",
                request,
                null);
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPagination(
            InvalidPaginationException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortField(
            InvalidSortFieldException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(InvalidSortDirectionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSortDirection(
            InvalidSortDirectionException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(InvalidStockAdjustmentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStockAdjustment(
            InvalidStockAdjustmentException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldErrorDetail> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                request,
                null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String message = "Invalid value for parameter '" + exception.getName() + "'";
        return buildResponse(HttpStatus.BAD_REQUEST, message, request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request,
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        String traceId = errorResponseFactory.currentTraceId(request);
        log.error(
                "event=unexpected_failure traceId={} path={} exceptionType={}",
                traceId,
                SafeLogValue.of(request.getRequestURI()),
                exception.getClass().getName());
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request,
                null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldErrorDetail> fieldErrors) {
        ErrorResponse body = errorResponseFactory.create(status, message, request, fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
