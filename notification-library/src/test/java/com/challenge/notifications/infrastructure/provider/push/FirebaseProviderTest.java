package com.challenge.notifications.infrastructure.provider.push;

import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.PushNotification;
import com.challenge.notifications.domain.model.SmsNotification;
import com.challenge.notifications.domain.valueobject.DeviceToken;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import com.challenge.notifications.infrastructure.configuration.PushConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebaseProviderTest {

    @Test
    void shouldSimulatePushDelivery() {
        FirebaseProvider provider = new FirebaseProvider(firebaseConfiguration());
        PushNotification push = PushNotification.create(
                new DeviceToken("device-token-123456789"),
                new MessageBody("Message")
        );

        ProviderResponse response = provider.send(push);

        assertThat(provider.channel()).isEqualTo(NotificationChannel.PUSH);
        assertThat(provider.providerName()).isEqualTo("Firebase");
        assertThat(response.accepted()).isTrue();
        assertThat(response.provider()).isEqualTo("Firebase");
    }

    @Test
    void shouldRejectNullConfiguration() {
        assertThatThrownBy(() -> new FirebaseProvider(null))
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Firebase configuration cannot be null");
    }

    @Test
    void shouldRejectConfigurationForUnknownProvider() {
        PushConfiguration configuration = mock(PushConfiguration.class);
        when(configuration.provider()).thenReturn(null);

        assertThatThrownBy(() -> new FirebaseProvider(configuration))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("FIREBASE");
    }

    @Test
    void shouldRejectNullNotification() {
        FirebaseProvider provider = new FirebaseProvider(firebaseConfiguration());

        assertThatThrownBy(() -> provider.send(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectNonPushNotification() {
        FirebaseProvider provider = new FirebaseProvider(firebaseConfiguration());
        SmsNotification sms = SmsNotification.create(
                new PhoneNumber("+50370000000"),
                new MessageBody("Message")
        );

        assertThatThrownBy(() -> provider.send(sms))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("only supports push");
    }

    private static PushConfiguration firebaseConfiguration() {
        return PushConfiguration.builder()
                .provider(PushConfiguration.Provider.FIREBASE)
                .projectId("project")
                .credentials("credentials")
                .build();
    }
}
