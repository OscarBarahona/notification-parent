package com.challenge.notifications.controller;

import com.challenge.notifications.application.facade.NotificationFacade;
import com.challenge.notifications.dto.NotificationResponse;
import com.challenge.notifications.mapper.NotificationRestMapper;
import com.challenge.notifications.security.SecurityConfiguration;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.enums.NotificationStatus;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfiguration.class)
class NotificationControllerTest {

    private static final String EMAIL_JSON = """
            {
              "channel": "EMAIL",
              "recipient": "customer@example.com",
              "subject": "Welcome",
              "message": "Account created"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationFacade notificationFacade;

    @MockitoBean
    private NotificationRestMapper mapper;

    private EmailNotification emailNotification;
    private NotificationResult notificationResult;
    private NotificationResponse response;

    @BeforeEach
    void setUp() {
        emailNotification = EmailNotification.create(
                new EmailAddress("customer@example.com"),
                new Subject("Welcome"),
                new MessageBody("Account created")
        );
        notificationResult = NotificationResult.sent(
                emailNotification,
                "SendGrid",
                "Accepted"
        );
        response = new NotificationResponse(
                emailNotification.id().value(),
                NotificationChannel.EMAIL,
                NotificationStatus.SENT,
                "SendGrid",
                Instant.now(),
                "Accepted",
                null
        );
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMAIL_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "NOTIFICATION_SENDER")
    void shouldSendNotificationSynchronously() throws Exception {
        when(mapper.toDomain(any())).thenReturn(emailNotification);
        when(notificationFacade.send(emailNotification)).thenReturn(notificationResult);
        when(mapper.toResponse(notificationResult)).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMAIL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId")
                        .value(emailNotification.id().value().toString()))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.provider").value("SendGrid"));

        verify(notificationFacade).send(emailNotification);
    }

    @Test
    @WithMockUser(roles = "NOTIFICATION_SENDER")
    void shouldSendNotificationAsynchronously() throws Exception {
        CompletableFuture<NotificationResult> future = new CompletableFuture<>();
        when(mapper.toDomain(any())).thenReturn(emailNotification);
        when(notificationFacade.sendAsync(emailNotification)).thenReturn(future);
        when(mapper.toResponse(notificationResult)).thenReturn(response);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/notifications/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMAIL_JSON))
                .andExpect(request().asyncStarted())
                .andReturn();

        future.complete(notificationResult);

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    @WithMockUser(roles = "NOTIFICATION_SENDER")
    void shouldReturnBeanValidationErrors() throws Exception {
        String invalidRequest = """
                {
                  "channel": "EMAIL",
                  "recipient": "",
                  "subject": "Welcome",
                  "message": ""
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.recipient").exists())
                .andExpect(jsonPath("$.fieldErrors.message").exists());
    }

    @Test
    @WithMockUser(roles = "NOTIFICATION_SENDER")
    void shouldReturnDomainValidationError() throws Exception {
        when(mapper.toDomain(any())).thenThrow(new ValidationException(
                ErrorCode.INVALID_EMAIL,
                "Invalid email"
        ));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMAIL_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EMAIL"))
                .andExpect(jsonPath("$.message").value("Invalid email"));
    }

    @Test
    @WithMockUser(roles = "NOTIFICATION_SENDER")
    void shouldReturnBadGatewayForProviderFailure() throws Exception {
        when(mapper.toDomain(any())).thenReturn(emailNotification);
        when(notificationFacade.send(emailNotification)).thenThrow(new ProviderException(
                ErrorCode.PROVIDER_ERROR,
                "Provider unavailable"
        ));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMAIL_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PROVIDER_ERROR"));
    }

    @Test
    @WithMockUser(roles = "NOTIFICATION_SENDER")
    void shouldReturnInternalErrorForConfigurationFailure() throws Exception {
        when(mapper.toDomain(any())).thenReturn(emailNotification);
        when(notificationFacade.send(emailNotification)).thenThrow(new ConfigurationException(
                ErrorCode.CONFIGURATION_ERROR,
                "Provider not configured"
        ));

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(EMAIL_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("CONFIGURATION_ERROR"));
    }

    @Test
    @WithMockUser(roles = "NOTIFICATION_SENDER")
    void shouldReturnMalformedRequestError() throws Exception {
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }
}
