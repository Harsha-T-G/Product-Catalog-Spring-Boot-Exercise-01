package com.codewalnut.productcatalog.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";
    public static final String TRACE_ATTRIBUTE = RequestTraceFilter.class.getName() + ".traceId";
    public static final String USERNAME_ATTRIBUTE = RequestTraceFilter.class.getName() + ".username";

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = selectTraceId(request.getHeader(TRACE_HEADER));
        request.setAttribute(TRACE_ATTRIBUTE, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        MDC.put(MDC_KEY, traceId);
        long startNanos = System.nanoTime();
        boolean downstreamFailure = false;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException exception) {
            downstreamFailure = true;
            throw exception;
        } finally {
            int completionStatus = downstreamFailure && response.getStatus() < 500
                    ? HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                    : response.getStatus();
            logCompletion(request, traceId, startNanos, completionStatus);
            MDC.remove(MDC_KEY);
        }
    }

    private void logCompletion(
            HttpServletRequest request,
            String traceId,
            long startNanos,
            int status) {
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        String message = "event=request_completed traceId=" + traceId
                + " method=" + SafeLogValue.of(request.getMethod())
                + " path=" + SafeLogValue.of(request.getRequestURI())
                + " status=" + status
                + " durationMs=" + durationMs
                + " username=" + SafeLogValue.of(
                        (String) request.getAttribute(USERNAME_ATTRIBUTE));
        if (status >= 500) {
            log.error(message);
        } else if (status >= 400) {
            log.warn(message);
        } else {
            log.info(message);
        }
    }

    private String selectTraceId(String callerTraceId) {
        if (callerTraceId != null && UUID_PATTERN.matcher(callerTraceId).matches()) {
            return callerTraceId;
        }
        return UUID.randomUUID().toString();
    }
}
