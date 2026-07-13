package com.challenge.notifications.integration;

import com.challenge.notifications.NotificationDemoApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SpringBootTest(
        classes = NotificationDemoApiApplication.class,
        properties = {
                "DEMO_API_USERNAME=integration-user",
                "DEMO_API_PASSWORD=integration-password"
        }
)
@AutoConfigureMockMvc
class NotificationDemoApiApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExecuteCompleteEmailFlow() throws Exception {
        String request = """
                {
                  "channel": "EMAIL",
                  "recipient": "customer@example.com",
                  "subject": "Welcome",
                  "message": "Account created"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .with(httpBasic("integration-user", "integration-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.provider").value("SendGrid"))
                .andExpect(jsonPath("$.errorCode").value(nullValue()));
    }


    @Test
    void shouldExecuteCompleteAsyncSmsFlow() throws Exception {
        String requestBody = """
                {
                  "channel": "SMS",
                  "recipient": "+50370000000",
                  "message": "Security code: 1234"
                }
                """;

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/notifications/async")
                        .with(httpBasic("integration-user", "integration-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.channel").value("SMS"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.provider").value("Twilio"));
    }

    @Test
    void shouldExposeOpenApiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Notification Demo API"));
    }

    @Test
    void shouldExposeHealthEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
