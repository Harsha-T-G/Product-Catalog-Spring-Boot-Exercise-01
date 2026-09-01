package com.codewalnut.productcatalog.dto;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldErrorDetail> fieldErrors;
    private final String errorReferenceId;

    public ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<FieldErrorDetail> fieldErrors) {
        this(timestamp, status, error, message, path, fieldErrors, null);
    }

    public ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            List<FieldErrorDetail> fieldErrors,
            String errorReferenceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
        this.errorReferenceId = errorReferenceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<FieldErrorDetail> getFieldErrors() {
        return fieldErrors;
    }

    public String getErrorReferenceId() {
        return errorReferenceId;
    }
}
