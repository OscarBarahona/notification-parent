package com.challenge.notifications.controller;

import com.challenge.notifications.application.facade.NotificationFacade;
import com.challenge.notifications.documentation.OpenApiConfiguration;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.dto.ApiErrorResponse;
import com.challenge.notifications.dto.NotificationRequest;
import com.challenge.notifications.dto.NotificationResponse;
import com.challenge.notifications.mapper.NotificationRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Send notifications through a unified API")
@RequiredArgsConstructor
@SecurityRequirement(name = OpenApiConfiguration.BASIC_AUTH_SCHEME)
public class NotificationController {

    private final NotificationFacade notificationFacade;
    private final NotificationRestMapper mapper;

    @PostMapping
    @Operation(
            summary = "Send a notification synchronously",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Notification processed"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid notification",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication required"),
                    @ApiResponse(responseCode = "502", description = "Provider failure")
            }
    )
    public ResponseEntity<NotificationResponse> send(
            @Valid @RequestBody NotificationRequest request
    ) {
        Notification notification = mapper.toDomain(request);
        NotificationResponse response = mapper.toResponse(
                notificationFacade.send(notification)
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/async")
    @Operation(
            summary = "Send a notification asynchronously",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Notification accepted for processing"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid notification",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication required")
            }
    )
    public CompletableFuture<ResponseEntity<NotificationResponse>> sendAsync(
            @Valid @RequestBody NotificationRequest request
    ) {
        Notification notification = mapper.toDomain(request);
        return notificationFacade.sendAsync(notification)
                .thenApply(mapper::toResponse)
                .thenApply(response -> ResponseEntity
                        .status(HttpStatus.ACCEPTED)
                        .body(response));
    }
}
