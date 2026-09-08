package com.indicadoresar.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundReturns404() {
        MockHttpServletRequest request = buildRequest("/test/not-found");

        ResponseEntity<ApiError> response =
                handler.handleResourceNotFound(
                        new ResourceNotFoundException("Item 42 not found"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Item 42 not found");
        assertThat(response.getBody().path()).isEqualTo("/test/not-found");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleValidationReturns400() throws Exception {
        MockHttpServletRequest request = buildRequest("/test/validate");

        MethodParameter param = buildMethodParameter();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "nameRequest");
        bindingResult.addError(new FieldError("nameRequest", "name", "must not be blank"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).contains("name");
        assertThat(response.getBody().path()).isEqualTo("/test/validate");
    }

    @Test
    void handleIllegalArgumentReturns400() {
        MockHttpServletRequest request = buildRequest("/test/illegal");

        ResponseEntity<ApiError> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad argument"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("bad argument");
        assertThat(response.getBody().path()).isEqualTo("/test/illegal");
    }

    @Test
    void handleConstraintViolationReturns400() {
        MockHttpServletRequest request = buildRequest("/test/constraint");

        ConstraintViolationException ex = new ConstraintViolationException("violation", Set.of());

        ResponseEntity<ApiError> response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("violation");
    }

    @Test
    void handleGenericReturns500() {
        MockHttpServletRequest request = buildRequest("/test/generic");

        ResponseEntity<ApiError> response = handler.handleGeneric(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
        assertThat(response.getBody().path()).isEqualTo("/test/generic");
    }

    private MockHttpServletRequest buildRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private MethodParameter buildMethodParameter() throws Exception {
        return new MethodParameter(
                DummyController.class.getMethod("dummy", String.class), 0);
    }

    static class DummyController {
        @GetMapping("/dummy")
        public void dummy(String name) {}
    }
}
