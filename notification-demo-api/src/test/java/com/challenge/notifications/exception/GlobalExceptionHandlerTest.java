package com.challenge.notifications.exception;

import com.challenge.notifications.dto.ApiErrorResponse;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.NotificationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = request();

    @Test
    void shouldUnwrapAsyncValidationException() {
        ResponseEntity<ApiErrorResponse> response = handler.handleCompletion(
                new CompletionException(new ValidationException(
                        ErrorCode.INVALID_EMAIL,
                        "Invalid email"
                )),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_EMAIL");
    }

    @Test
    void shouldUnwrapAsyncProviderException() {
        ResponseEntity<ApiErrorResponse> response = handler.handleCompletion(
                new CompletionException(new ProviderException(
                        ErrorCode.PROVIDER_ERROR,
                        "Provider failed"
                )),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void shouldUnwrapAsyncConfigurationException() {
        ResponseEntity<ApiErrorResponse> response = handler.handleCompletion(
                new CompletionException(new ConfigurationException(
                        ErrorCode.CONFIGURATION_ERROR,
                        "Configuration failed"
                )),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("CONFIGURATION_ERROR");
    }

    @Test
    void shouldHideUnexpectedAsyncFailureDetails() {
        ResponseEntity<ApiErrorResponse> response = handler.handleCompletion(
                new CompletionException(new IllegalStateException("Sensitive detail")),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).doesNotContain("Sensitive detail");
    }

    @Test
    void shouldHandleGenericNotificationException() {
        NotificationException exception = new TestNotificationException(
                ErrorCode.INVALID_NOTIFICATION,
                "Generic notification failure"
        );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleNotificationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_NOTIFICATION");
    }

    @Test
    void shouldSanitizeUnexpectedRuntimeException() {
        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpected(
                new IllegalStateException("Database password leaked"),
                request
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/notifications");
    }

    private static MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/notifications");
        return request;
    }

    private static final class TestNotificationException extends NotificationException {

        private TestNotificationException(ErrorCode errorCode, String message) {
            super(errorCode, message);
        }
    }
}
