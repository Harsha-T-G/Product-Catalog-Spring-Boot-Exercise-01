package com.codewalnut.productcatalog.security;

import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessIntegrationTest extends PostgreSqlTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenNoCredentials_whenGetProducts_thenReturns401() throws Exception {
        String traceId = "c761fef4-11c0-4cb8-96cd-2f9c9266b22d";

        // Act & Assert
        mockMvc.perform(get("/api/products").header(RequestTraceFilter.TRACE_HEADER, traceId))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic")))
                .andExpect(header().string(RequestTraceFilter.TRACE_HEADER, traceId))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/api/products"))
                .andExpect(jsonPath("$.traceId").value(traceId))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void givenNoCredentials_whenGetApiInfo_thenReturns200() throws Exception {
        // Act & Assert
        MvcResult result = mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andReturn();
        assertDoesNotThrow(() -> UUID.fromString(
                result.getResponse().getHeader(RequestTraceFilter.TRACE_HEADER)));
    }

    @Test
    void givenNoCredentials_whenGetActuatorHealth_thenReturns200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/actuator/info",
            "/v3/api-docs",
            "/swagger-ui.html",
            "/not-mapped"
    })
    void givenNoCredentials_whenGetNonPublicPath_thenReturns401(String path) throws Exception {
        // Act & Assert
        mockMvc.perform(get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic")));
    }
}
