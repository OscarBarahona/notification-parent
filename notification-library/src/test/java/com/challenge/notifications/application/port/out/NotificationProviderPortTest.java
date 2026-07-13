package com.challenge.notifications.application.port.out;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ProviderException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationProviderPortTest {

    @Test
    void shouldCreateAcceptedResponse() {
        var response = NotificationProviderPort.ProviderResponse.accepted(
                "  SendGrid  ",
                "  Accepted  "
        );

        assertThat(response.accepted()).isTrue();
        assertThat(response.provider()).isEqualTo("SendGrid");
        assertThat(response.detail()).isEqualTo("Accepted");
        assertThat(response.errorCode()).isNull();
    }

    @Test
    void shouldCreateRejectedResponse() {
        var response = NotificationProviderPort.ProviderResponse.rejected(
                "Twilio",
                "Rejected",
                ErrorCode.PROVIDER_ERROR
        );

        assertThat(response.accepted()).isFalse();
        assertThat(response.errorCode()).isEqualTo(ErrorCode.PROVIDER_ERROR);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldRejectInvalidProviderName(String provider) {
        assertThatThrownBy(() ->
                NotificationProviderPort.ProviderResponse.accepted(provider, "Accepted")
        )
                .isInstanceOf(ProviderException.class)
                .extracting(exception -> ((ProviderException) exception).getErrorCode())
                .isEqualTo(ErrorCode.PROVIDER_ERROR);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldRejectInvalidDetail(String detail) {
        assertThatThrownBy(() ->
                NotificationProviderPort.ProviderResponse.accepted("SendGrid", detail)
        ).isInstanceOf(ProviderException.class);
    }

    @Test
    void shouldRejectAcceptedResponseWithErrorCode() {
        assertThatThrownBy(() -> new NotificationProviderPort.ProviderResponse(
                true,
                "SendGrid",
                "Accepted",
                ErrorCode.PROVIDER_ERROR
        )).isInstanceOf(ProviderException.class);
    }

    @Test
    void shouldRejectFailedResponseWithoutErrorCode() {
        assertThatThrownBy(() -> new NotificationProviderPort.ProviderResponse(
                false,
                "SendGrid",
                "Rejected",
                null
        )).isInstanceOf(ProviderException.class);
    }
}
