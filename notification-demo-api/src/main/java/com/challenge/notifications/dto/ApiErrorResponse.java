package com.challenge.notifications.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(description = "Standard API error response")
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
    public ApiErrorResponse {
        fieldErrors = fieldErrors == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }
}
