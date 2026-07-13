package com.challenge.notifications.domain.exception;

import com.challenge.notifications.domain.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationExceptionTest {

    @Test
    void shouldExposeValidationErrorCodeAndCause() {
        IllegalArgumentException cause = new IllegalArgumentException("invalid");
        ValidationException exception = new ValidationException(
                ErrorCode.INVALID_EMAIL,
                "Invalid email",
                cause
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_EMAIL);
        assertThat(exception.getMessage()).isEqualTo("Invalid email");
        assertThat(exception.getCause()).isSameAs(cause);
    }

    @Test
    void shouldCreateProviderExceptionWithAndWithoutCause() {
        ProviderException simple = new ProviderException(
                ErrorCode.PROVIDER_ERROR,
                "Provider error"
        );
        RuntimeException cause = new RuntimeException("timeout");
        ProviderException withCause = new ProviderException(
                ErrorCode.PROVIDER_ERROR,
                "Provider timeout",
                cause
        );

        assertThat(simple.getErrorCode()).isEqualTo(ErrorCode.PROVIDER_ERROR);
        assertThat(simple.getCause()).isNull();
        assertThat(withCause.getCause()).isSameAs(cause);
    }

    @Test
    void shouldCreateConfigurationExceptionWithAndWithoutCause() {
        ConfigurationException simple = new ConfigurationException(
                ErrorCode.CONFIGURATION_ERROR,
                "Missing configuration"
        );
        RuntimeException cause = new RuntimeException("invalid key");
        ConfigurationException withCause = new ConfigurationException(
                ErrorCode.CONFIGURATION_ERROR,
                "Invalid configuration",
                cause
        );

        assertThat(simple.getErrorCode()).isEqualTo(ErrorCode.CONFIGURATION_ERROR);
        assertThat(withCause.getCause()).isSameAs(cause);
    }

    @Test
    void shouldRejectNullErrorCode() {
        assertThatThrownBy(() -> new ValidationException(null, "Invalid"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("errorCode cannot be null");
    }
}
