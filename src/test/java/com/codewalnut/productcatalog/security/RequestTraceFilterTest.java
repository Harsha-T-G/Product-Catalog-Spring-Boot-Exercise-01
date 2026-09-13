package com.codewalnut.productcatalog.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestTraceFilterTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void givenValidIncomingUuid_whenFiltering_thenReusesItInMdcRequestAndResponse() throws Exception {
        RequestTraceFilter filter = new RequestTraceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String incomingTraceId = "A4E78A4B-7CA3-4AD8-9F6B-81D923DE431C";
        request.addHeader(RequestTraceFilter.TRACE_HEADER, incomingTraceId);

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals(incomingTraceId, MDC.get(RequestTraceFilter.MDC_KEY));
            assertEquals(incomingTraceId, servletRequest.getAttribute(RequestTraceFilter.TRACE_ATTRIBUTE));
        });

        assertEquals(incomingTraceId, response.getHeader(RequestTraceFilter.TRACE_HEADER));
        assertNull(MDC.get(RequestTraceFilter.MDC_KEY));
    }

    @Test
    void givenInvalidIncomingValue_whenFiltering_thenReplacesItWithUuid() throws Exception {
        RequestTraceFilter filter = new RequestTraceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestTraceFilter.TRACE_HEADER, " invalid trace\nvalue ");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        String selected = response.getHeader(RequestTraceFilter.TRACE_HEADER);
        assertNotEquals(" invalid trace\nvalue ", selected);
        assertDoesNotThrow(() -> UUID.fromString(selected));
        assertNull(MDC.get(RequestTraceFilter.MDC_KEY));
    }

    @Test
    void givenDownstreamFailure_whenFiltering_thenStillClearsMdc() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestTraceFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        RequestTraceFilter filter = new RequestTraceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            assertThrows(ServletException.class, () -> filter.doFilter(
                    request,
                    response,
                    (servletRequest, servletResponse) -> {
                        throw new ServletException("test failure");
                    }));

            assertNull(MDC.get(RequestTraceFilter.MDC_KEY));
            assertEquals(1, appender.list.size());
            assertEquals(Level.ERROR, appender.list.get(0).getLevel());
            assertTrue(appender.list.get(0).getFormattedMessage().contains("status=500"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void givenConsecutiveRequests_whenFiltering_thenEachRequestGetsIsolatedTrace() throws Exception {
        RequestTraceFilter filter = new RequestTraceFilter();
        AtomicReference<String> firstTrace = new AtomicReference<>();
        AtomicReference<String> secondTrace = new AtomicReference<>();

        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/first"),
                new MockHttpServletResponse(),
                (request, response) -> firstTrace.set(MDC.get(RequestTraceFilter.MDC_KEY)));
        assertNull(MDC.get(RequestTraceFilter.MDC_KEY));

        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/second"),
                new MockHttpServletResponse(),
                (request, response) -> secondTrace.set(MDC.get(RequestTraceFilter.MDC_KEY)));

        assertNotEquals(firstTrace.get(), secondTrace.get());
        assertNull(MDC.get(RequestTraceFilter.MDC_KEY));
    }

    @Test
    void givenCompletedRequest_whenFiltering_thenLogsOneSafeCompletionEvent() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestTraceFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            RequestTraceFilter filter = new RequestTraceFilter();
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/products");
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.addHeader(RequestTraceFilter.TRACE_HEADER, "7046bd93-568f-49ae-ac1d-9d5be793f720");

            filter.doFilter(request, response, (servletRequest, servletResponse) -> {
                servletRequest.setAttribute(RequestTraceFilter.USERNAME_ATTRIBUTE, "editor\nforged");
                ((MockHttpServletResponse) servletResponse).setStatus(201);
            });

            assertEquals(1, appender.list.size());
            ILoggingEvent event = appender.list.get(0);
            assertEquals(Level.INFO, event.getLevel());
            String message = event.getFormattedMessage();
            assertTrue(message.contains("event=request_completed"));
            assertTrue(message.contains("traceId=7046bd93-568f-49ae-ac1d-9d5be793f720"));
            assertTrue(message.contains("method=POST"));
            assertTrue(message.contains("path=/api/products"));
            assertTrue(message.contains("status=201"));
            assertTrue(message.matches(".*durationMs=\\d+.*"));
            assertTrue(message.contains("username=editor_forged"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
