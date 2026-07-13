package com.challenge.notifications.application.usecase;

import com.challenge.notifications.application.mapper.NotificationResultMapper;
import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.application.port.out.NotificationProviderPort.ProviderResponse;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.enums.NotificationStatus;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.model.SmsNotification;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import com.challenge.notifications.domain.valueobject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseTest {

    @Mock
    private NotificationProviderPort emailProvider;

    private EmailNotification emailNotification;

    @BeforeEach
    void setUp() {
        emailNotification = EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Message")
        );
    }

    @Test
    void shouldSendNotificationSuccessfully() {
        configureValidEmailProvider();
        when(emailProvider.send(emailNotification))
                .thenReturn(ProviderResponse.accepted("SendGrid", "Accepted"));

        var useCase = new SendNotificationUseCase(List.of(emailProvider));
        NotificationResult result = useCase.send(emailNotification);

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.provider()).isEqualTo("SendGrid");
        verify(emailProvider).send(emailNotification);
    }

    @Test
    void shouldReturnFailedResultWhenProviderRejectsNotification() {
        configureValidEmailProvider();
        when(emailProvider.send(emailNotification)).thenReturn(
                ProviderResponse.rejected(
                        "SendGrid",
                        "Rejected",
                        ErrorCode.PROVIDER_ERROR
                )
        );

        var useCase = new SendNotificationUseCase(List.of(emailProvider));
        NotificationResult result = useCase.send(emailNotification);

        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.errorCode()).contains(ErrorCode.PROVIDER_ERROR);
    }

    @Test
    void shouldRejectNullNotification() {
        configureValidEmailProvider();
        var useCase = new SendNotificationUseCase(List.of(emailProvider));

        assertThatThrownBy(() -> useCase.send(null))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldRejectMissingProviderForChannel() {
        configureValidEmailProvider();
        var useCase = new SendNotificationUseCase(List.of(emailProvider));
        var sms = SmsNotification.create(
                new PhoneNumber("+50370000000"),
                new MessageBody("Message")
        );

        assertThatThrownBy(() -> useCase.send(sms))
                .isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("SMS");
    }

    @Test
    void shouldRejectNullProviderResponse() {
        configureValidEmailProvider();
        when(emailProvider.send(emailNotification)).thenReturn(null);
        var useCase = new SendNotificationUseCase(List.of(emailProvider));

        assertThatThrownBy(() -> useCase.send(emailNotification))
                .isInstanceOf(ProviderException.class)
                .hasMessage("Provider response cannot be null");
    }

    @Test
    void shouldRejectMismatchedProviderResponse() {
        configureValidEmailProvider();
        when(emailProvider.send(emailNotification))
                .thenReturn(ProviderResponse.accepted("Mailgun", "Accepted"));
        var useCase = new SendNotificationUseCase(List.of(emailProvider));

        assertThatThrownBy(() -> useCase.send(emailNotification))
                .isInstanceOf(ProviderException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void shouldRethrowNotificationExceptionFromProvider() {
        configureValidEmailProvider();
        var failure = new ProviderException(ErrorCode.PROVIDER_ERROR, "Unavailable");
        when(emailProvider.send(emailNotification)).thenThrow(failure);
        var useCase = new SendNotificationUseCase(List.of(emailProvider));

        assertThatThrownBy(() -> useCase.send(emailNotification))
                .isSameAs(failure);
    }

    @Test
    void shouldWrapUnexpectedRuntimeException() {
        configureValidEmailProvider();
        var failure = new IllegalStateException("Unexpected");
        when(emailProvider.send(emailNotification)).thenThrow(failure);
        var useCase = new SendNotificationUseCase(List.of(emailProvider));

        assertThatThrownBy(() -> useCase.send(emailNotification))
                .isInstanceOf(ProviderException.class)
                .hasCause(failure)
                .hasMessageContaining("SendGrid");
    }

    @Test
    void shouldRejectNullProviderCollection() {
        assertThatThrownBy(() -> new SendNotificationUseCase(null))
                .isInstanceOf(ConfigurationException.class);
    }

    @Test
    void shouldRejectEmptyProviderCollection() {
        assertThatThrownBy(() -> new SendNotificationUseCase(List.of()))
                .isInstanceOf(ConfigurationException.class);
    }

    @Test
    void shouldRejectNullProviderElement() {
        assertThatThrownBy(() -> new SendNotificationUseCase(
                java.util.Arrays.asList((NotificationProviderPort) null)
        )).isInstanceOf(ConfigurationException.class);
    }

    @Test
    void shouldRejectProviderWithoutChannel() {
        when(emailProvider.channel()).thenReturn(null);

        assertThatThrownBy(() -> new SendNotificationUseCase(List.of(emailProvider)))
                .isInstanceOf(ConfigurationException.class);
    }

    @Test
    void shouldRejectProviderWithoutName() {
        when(emailProvider.channel())
                .thenReturn(NotificationChannel.EMAIL);

        when(emailProvider.providerName())
                .thenReturn(" ");

        assertThatThrownBy(
                () -> new SendNotificationUseCase(List.of(emailProvider))
        )
                .isInstanceOf(ConfigurationException.class)
                .hasMessage("Configured provider name cannot be null or blank");
    }

    @Test
    void shouldRejectDuplicateProvidersForSameChannel() {
        configureValidEmailProvider();
        NotificationProviderPort secondProvider = org.mockito.Mockito.mock(
                NotificationProviderPort.class
        );
        when(secondProvider.channel()).thenReturn(NotificationChannel.EMAIL);
        when(secondProvider.providerName()).thenReturn("Mailgun");

        assertThatThrownBy(() -> new SendNotificationUseCase(
                List.of(emailProvider, secondProvider)
        )).isInstanceOf(ConfigurationException.class)
                .hasMessageContaining("EMAIL");
    }

    @Test
    void shouldRejectNullMapper() {
        configureValidEmailProvider();
        assertThatThrownBy(() -> new SendNotificationUseCase(
                List.of(emailProvider),
                null
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldUseInjectedMapper() {
        configureValidEmailProvider();
        NotificationResultMapper mapper = org.mockito.Mockito.mock(
                NotificationResultMapper.class
        );
        ProviderResponse response = ProviderResponse.accepted("SendGrid", "Accepted");
        NotificationResult expected = NotificationResult.sent(
                emailNotification,
                "SendGrid",
                "Accepted"
        );
        when(emailProvider.send(emailNotification)).thenReturn(response);
        when(mapper.toDomain(emailNotification, response)).thenReturn(expected);

        var useCase = new SendNotificationUseCase(List.of(emailProvider), mapper);

        assertThat(useCase.send(emailNotification)).isSameAs(expected);
        verify(mapper).toDomain(emailNotification, response);
    }
    private void configureValidEmailProvider() {
        when(emailProvider.channel()).thenReturn(NotificationChannel.EMAIL);
        when(emailProvider.providerName()).thenReturn("SendGrid");
    }

}
