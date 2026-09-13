package com.codewalnut.productcatalog.controller;

import com.codewalnut.productcatalog.config.SecurityConfig;
import com.codewalnut.productcatalog.exception.ErrorResponseFactory;
import com.codewalnut.productcatalog.security.RestAccessDeniedHandler;
import com.codewalnut.productcatalog.security.RestAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InfoController.class)
@Import({
        SecurityConfig.class,
        ErrorResponseFactory.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        InfoControllerTest.TestIdentityConfiguration.class
})
@TestPropertySource(properties = {
        "spring.application.name=Product Catalog API",
        "info.app.version=1.0.0"
})
class InfoControllerTest {

    @TestConfiguration
    static class TestIdentityConfiguration {

        @Bean
        UserDetailsService testUserDetailsService() {
            return username -> {
                throw new UsernameNotFoundException("Invalid credentials");
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenDefaultConfiguration_whenGetInfo_thenReturnsConfiguredMetadata() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Product Catalog API"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
