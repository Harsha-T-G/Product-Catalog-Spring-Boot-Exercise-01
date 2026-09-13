package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.dto.ErrorResponse;
import com.codewalnut.productcatalog.exception.ErrorResponseFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory errorResponseFactory;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper, ErrorResponseFactory errorResponseFactory) {
        this.objectMapper = objectMapper;
        this.errorResponseFactory = errorResponseFactory;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException, ServletException {
        String traceId = errorResponseFactory.currentTraceId(request);
        log.warn(
                "event=authentication_failure traceId={} path={} outcome=unauthorized",
                traceId,
                SafeLogValue.of(request.getRequestURI()));
        ErrorResponse body = errorResponseFactory.create(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required",
                request,
                null);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Product Catalog\"");
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
