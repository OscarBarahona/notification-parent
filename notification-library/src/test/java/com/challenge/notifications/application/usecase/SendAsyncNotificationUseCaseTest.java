package com.challenge.notifications.application.usecase;

import com.challenge.notifications.application.port.in.SendNotificationInputPort;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ProviderException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SendAsyncNotificationUseCaseTest {

    private final Executor directExecutor = Runnable::run;
    private SendNotificationInputPort synchronousUseCase;
    private EmailNotification notification;

    @BeforeEach
    void setUp() {
        synchronousUseCase = mock(SendNotificationInputPort.class);
        notification = EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Message")
        );
    }

    @Test
    void shouldSendNotificationAsynchronously() {
        NotificationResult expected = NotificationResult.sent(
                notification,
                "SendGrid",
                "Accepted"
        );
        when(synchronousUseCase.send(notification)).thenReturn(expected);

        var useCase = new SendAsyncNotificationUseCase(
                synchronousUseCase,
                directExecutor
        );

        assertThat(useCase.sendAsync(notification).join()).isSameAs(expected);
    }

    @Test
    void shouldCompleteExceptionallyWhenSynchronousUseCaseFails() {
        ProviderException failure = new ProviderException(
                ErrorCode.PROVIDER_ERROR,
                "Unavailable"
        );
        when(synchronousUseCase.send(notification)).thenThrow(failure);

        var useCase = new SendAsyncNotificationUseCase(
                synchronousUseCase,
                directExecutor
        );

        assertThatThrownBy(() -> useCase.sendAsync(notification).join())
                .isInstanceOf(CompletionException.class)
                .hasCause(failure);
    }

    @Test
    void shouldRejectNullSynchronousUseCase() {
        assertThatThrownBy(() -> new SendAsyncNotificationUseCase(
                null,
                directExecutor
        )).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullExecutor() {
        assertThatThrownBy(() -> new SendAsyncNotificationUseCase(
                synchronousUseCase,
                null
        )).isInstanceOf(NullPointerException.class);
    }
}
