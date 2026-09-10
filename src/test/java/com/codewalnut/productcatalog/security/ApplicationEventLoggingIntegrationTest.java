package com.codewalnut.productcatalog.security;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.codewalnut.productcatalog.entity.ProductEntity;
import com.codewalnut.productcatalog.repository.AppUserRepository;
import com.codewalnut.productcatalog.repository.ProductRepository;
import com.codewalnut.productcatalog.service.ProductService;
import com.codewalnut.productcatalog.service.UserService;
import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationEventLoggingIntegrationTest extends PostgreSqlTestSupport {

    private static final String PASSWORD_MARKER = "never-log-this-password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void cleanData() {
        productRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void givenSuccessfulStateChanges_whenRequestsComplete_thenSafeDomainEventsAreLogged() throws Exception {
        Logger productLogger = (Logger) LoggerFactory.getLogger(ProductService.class);
        Logger userLogger = (Logger) LoggerFactory.getLogger(UserService.class);
        ListAppender<ILoggingEvent> productEvents = attach(productLogger);
        ListAppender<ILoggingEvent> userEvents = attach(userLogger);
        try {
            mockMvc.perform(post("/api/products")
                            .with(user("editor-user").roles("EDITOR"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validProductJson("LOG-CREATE")))
                    .andExpect(status().isCreated());

            ProductEntity stockProduct = productRepository.saveAndFlush(new ProductEntity(
                    UUID.randomUUID(),
                    "LOG-STOCK",
                    "Logging Stock Product",
                    "Observability",
                    new BigDecimal("29.99"),
                    5,
                    true));
            mockMvc.perform(patch("/api/products/{id}/stock", stockProduct.getId())
                            .with(user("editor-user").roles("EDITOR"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"adjustment\":2}"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/admin/users")
                            .with(user("admin-user").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createUserJson()))
                    .andExpect(status().isCreated());
            mockMvc.perform(patch("/api/admin/users/{username}/enabled", "log-viewer")
                            .with(user("admin-user").roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"enabled\":false}"))
                    .andExpect(status().isOk());

            List<String> productMessages = messages(productEvents);
            List<String> userMessages = messages(userEvents);
            assertTrue(productMessages.stream().anyMatch(message -> message.contains("event=product_created")));
            assertTrue(productMessages.stream().anyMatch(message -> message.contains("event=stock_adjusted")));
            assertTrue(userMessages.stream().anyMatch(message -> message.contains("event=user_created")));
            assertTrue(userMessages.stream().anyMatch(message -> message.contains("event=user_enabled_changed")));
            assertFalse(userMessages.stream().anyMatch(message -> message.contains(PASSWORD_MARKER)));
            assertTrue(productEvents.list.stream()
                    .allMatch(event -> event.getMDCPropertyMap().containsKey(RequestTraceFilter.MDC_KEY)));
            assertTrue(userEvents.list.stream()
                    .allMatch(event -> event.getMDCPropertyMap().containsKey(RequestTraceFilter.MDC_KEY)));
        } finally {
            detach(productLogger, productEvents);
            detach(userLogger, userEvents);
        }
    }

    @Test
    void givenAuthenticationAndAuthorizationFailures_whenRequestsComplete_thenSecurityLogsStaySafe()
            throws Exception {
        Logger authenticationLogger = (Logger) LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);
        Logger deniedLogger = (Logger) LoggerFactory.getLogger(RestAccessDeniedHandler.class);
        Logger requestLogger = (Logger) LoggerFactory.getLogger(RequestTraceFilter.class);
        ListAppender<ILoggingEvent> authenticationEvents = attach(authenticationLogger);
        ListAppender<ILoggingEvent> deniedEvents = attach(deniedLogger);
        ListAppender<ILoggingEvent> requestEvents = attach(requestLogger);
        String bodyMarker = "DO-NOT-LOG-COMPLETE-BODY";
        try {
            mockMvc.perform(get("/api/products")
                            .with(httpBasic("missing-log-user", PASSWORD_MARKER)))
                    .andExpect(status().isUnauthorized());
            mockMvc.perform(post("/api/products")
                            .with(user("viewer-user").roles("VIEWER"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validProductJson(bodyMarker)))
                    .andExpect(status().isForbidden());

            List<String> authenticationMessages = messages(authenticationEvents);
            List<String> deniedMessages = messages(deniedEvents);
            List<String> requestMessages = messages(requestEvents);
            assertTrue(authenticationMessages.stream()
                    .anyMatch(message -> message.contains("event=authentication_failure")));
            assertTrue(deniedMessages.stream().anyMatch(message -> message.contains("event=access_denied")));
            assertTrue(requestMessages.stream()
                    .anyMatch(message -> message.contains("status=403")
                            && message.contains("username=viewer-user")));
            assertFalse(authenticationMessages.stream().anyMatch(message -> message.contains(PASSWORD_MARKER)));
            assertFalse(authenticationMessages.stream().anyMatch(message -> message.contains("Authorization")));
            assertFalse(deniedMessages.stream().anyMatch(message -> message.contains(bodyMarker)));
            assertFalse(deniedMessages.stream().anyMatch(message -> message.contains("AccessDeniedException")));
            assertFalse(requestMessages.stream().anyMatch(message -> message.contains(bodyMarker)));
        } finally {
            detach(authenticationLogger, authenticationEvents);
            detach(deniedLogger, deniedEvents);
            detach(requestLogger, requestEvents);
        }
    }

    private ListAppender<ILoggingEvent> attach(Logger logger) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void detach(Logger logger, ListAppender<ILoggingEvent> appender) {
        logger.detachAppender(appender);
        appender.stop();
    }

    private List<String> messages(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    private String validProductJson(String sku) {
        return """
                {
                  "sku": "%s",
                  "name": "Logging Product",
                  "category": "Observability",
                  "price": 19.99,
                  "stockQuantity": 5,
                  "active": true
                }
                """.formatted(sku);
    }

    private String createUserJson() {
        return """
                {
                  "username": "log-viewer",
                  "password": "%s",
                  "roles": ["VIEWER"]
                }
                """.formatted(PASSWORD_MARKER);
    }
}
