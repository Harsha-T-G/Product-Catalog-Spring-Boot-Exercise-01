package com.codewalnut.productcatalog.exception;

import com.codewalnut.productcatalog.dto.ErrorResponse;
import com.codewalnut.productcatalog.dto.FieldErrorDetail;
import com.codewalnut.productcatalog.security.RequestTraceFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class ErrorResponseFactory {

    public ErrorResponse create(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors,
                currentTraceId(request));
    }

    public String currentTraceId(HttpServletRequest request) {
        Object requestTraceId = request.getAttribute(RequestTraceFilter.TRACE_ATTRIBUTE);
        if (requestTraceId instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }
        String mdcTraceId = MDC.get(RequestTraceFilter.MDC_KEY);
        if (mdcTraceId != null && !mdcTraceId.isBlank()) {
            return mdcTraceId;
        }
        String generatedTraceId = UUID.randomUUID().toString();
        request.setAttribute(RequestTraceFilter.TRACE_ATTRIBUTE, generatedTraceId);
        return generatedTraceId;
    }
}
