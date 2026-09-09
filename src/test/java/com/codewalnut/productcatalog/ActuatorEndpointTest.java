package com.codewalnut.productcatalog;

import com.codewalnut.productcatalog.support.PostgreSqlTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActuatorEndpointTest extends PostgreSqlTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenRunningApplication_whenHealthEndpointCalled_thenReturnsUpWithDatabase() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    void givenRunningApplication_whenInfoEndpointCalled_thenReturnsApplicationMetadata() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("Product Catalog API"))
                .andExpect(jsonPath("$.app.version").value("1.0.0"));
    }

    @Test
    void givenRunningApplication_whenEnvEndpointCalled_thenIsNotExposed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isNotFound());
    }
}
