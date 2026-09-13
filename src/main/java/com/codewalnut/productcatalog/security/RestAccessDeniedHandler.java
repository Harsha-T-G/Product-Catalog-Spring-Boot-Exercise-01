package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.dto.ErrorResponse;
import com.codewalnut.productcatalog.exception.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory errorResponseFactory;

    public RestAccessDeniedHandler(ObjectMapper objectMapper, ErrorResponseFactory errorResponseFactory) {
        this.objectMapper = objectMapper;
        this.errorResponseFactory = errorResponseFactory;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication == null ? null : authentication.getName();
        String traceId = errorResponseFactory.currentTraceId(request);
        log.warn(
                "event=access_denied traceId={} path={} username={} outcome=forbidden",
                traceId,
                SafeLogValue.of(request.getRequestURI()),
                SafeLogValue.of(username));
        ErrorResponse body = errorResponseFactory.create(
                HttpStatus.FORBIDDEN,
                "Access is denied",
                request,
                null);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
