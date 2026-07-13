package com.challenge.notifications.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiErrorResponseTest {

    @Test
    void shouldDefensivelyCopyFieldErrors() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("message", "must not be blank");

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                400,
                "Bad Request",
                "REQUEST_VALIDATION_ERROR",
                "Invalid request",
                "/api/v1/notifications",
                fields
        );
        fields.put("recipient", "must not be blank");

        assertThat(response.fieldErrors()).containsOnlyKeys("message");
        assertThatThrownBy(() -> response.fieldErrors().put("other", "error"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldReplaceNullFieldErrorsWithEmptyMap() {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                500,
                "Internal Server Error",
                "INTERNAL_ERROR",
                "Unexpected",
                "/api/v1/notifications",
                null
        );

        assertThat(response.fieldErrors()).isEmpty();
    }
}
