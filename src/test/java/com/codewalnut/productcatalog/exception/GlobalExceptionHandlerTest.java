package com.codewalnut.productcatalog.exception;

import com.codewalnut.productcatalog.dto.ErrorResponse;
import com.codewalnut.productcatalog.dto.FieldErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import com.codewalnut.productcatalog.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/products");
    }

    @Test
    void givenProductNotFound_whenHandle_thenReturns404ErrorEnvelope() {
        // Arrange
        UUID id = UUID.randomUUID();

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleProductNotFound(
                new ProductNotFoundException(id), request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("/api/products", body.getPath());
        assertNull(body.getFieldErrors());
    }

    @Test
    void givenDuplicateSku_whenHandle_thenReturns409ErrorEnvelope() {
        // Act
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateSku(
                new DuplicateSkuException("SKU-001"), request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void givenValidationFailure_whenHandle_thenReturns400WithFieldErrors() throws NoSuchMethodException {
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "productRequest");
        bindingResult.addError(new FieldError("productRequest", "sku", "must not be blank"));
        MethodParameter methodParameter = new MethodParameter(
                ValidationTarget.class.getDeclaredMethod("accept", ProductRequest.class), 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        FieldErrorDetail fieldError = response.getBody().getFieldErrors().get(0);
        assertEquals("sku", fieldError.getField());
        assertEquals("must not be blank", fieldError.getMessage());
    }

    private static class ValidationTarget {
        @SuppressWarnings("unused")
        void accept(ProductRequest request) {
        }
    }

    @Test
    void givenInvalidUuid_whenHandleTypeMismatch_thenReturns400() {
        // Arrange
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "bad-id", UUID.class, "id", null, new IllegalArgumentException("Invalid UUID"));

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().getMessage().isBlank());
    }

    @Test
    void givenUnsupportedMethod_whenHandle_thenReturns405() {
        // Act
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("PATCH"), request);

        // Assert
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(405, response.getBody().getStatus());
    }

    @Test
    void givenUnexpectedException_whenHandle_thenReturns500WithoutInternalDetails() {
        // Act
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(
                new RuntimeException("database password leaked"), request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
