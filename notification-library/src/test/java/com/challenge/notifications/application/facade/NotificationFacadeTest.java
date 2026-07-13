package com.challenge.notifications.application.facade;

import com.challenge.notifications.application.port.in.SendNotificationInputPort;
import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.application.usecase.SendAsyncNotificationUseCase;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class NotificationFacadeTest {

    private SendNotificationInputPort synchronousUseCase;
    private SendAsyncNotificationUseCase asynchronousUseCase;
    private EmailNotification notification;
    private NotificationResult result;

    @BeforeEach
    void setUp() {
        synchronousUseCase = mock(SendNotificationInputPort.class);
        asynchronousUseCase = mock(SendAsyncNotificationUseCase.class);
        notification = EmailNotification.create(
                new EmailAddress("user@example.com"),
                new Subject("Subject"),
                new MessageBody("Message")
        );
        result = NotificationResult.sent(notification, "SendGrid", "Accepted");
    }

    @Test
    void shouldDelegateSynchronousSend() {
        when(synchronousUseCase.send(notification)).thenReturn(result);
        var facade = new NotificationFacade(synchronousUseCase, asynchronousUseCase);

        assertThat(facade.send(notification)).isSameAs(result);
        verify(synchronousUseCase).send(notification);
    }

    @Test
    void shouldDelegateAsynchronousSend() {
        CompletableFuture<NotificationResult> future =
                CompletableFuture.completedFuture(result);
        when(asynchronousUseCase.sendAsync(notification)).thenReturn(future);
        var facade = new NotificationFacade(synchronousUseCase, asynchronousUseCase);

        assertThat(facade.sendAsync(notification)).isSameAs(future);
        verify(asynchronousUseCase).sendAsync(notification);
    }

    @Test
    void shouldCreateOperationalFacadeWithExplicitExecutor() {
        NotificationProviderPort provider = provider();
        Executor directExecutor = Runnable::run;
        var facade = NotificationFacade.create(List.of(provider), directExecutor);

        assertThat(facade.send(notification).isSuccess()).isTrue();
        assertThat(facade.sendAsync(notification).join().isSuccess()).isTrue();
    }

    @Test
    void shouldCreateOperationalFacadeWithDefaultExecutor() {
        var facade = NotificationFacade.create(List.of(provider()));

        assertThat(facade.sendAsync(notification).join().isSuccess()).isTrue();
    }

    @Test
    void shouldRejectNullSynchronousUseCase() {
        assertThatThrownBy(() -> new NotificationFacade(null, asynchronousUseCase))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullAsynchronousUseCase() {
        assertThatThrownBy(() -> new NotificationFacade(synchronousUseCase, null))
                .isInstanceOf(NullPointerException.class);
    }

    private NotificationProviderPort provider() {
        return new NotificationProviderPort() {
            @Override
            public NotificationChannel channel() {
                return NotificationChannel.EMAIL;
            }

            @Override
            public String providerName() {
                return "SendGrid";
            }

            @Override
            public ProviderResponse send(
                    com.challenge.notifications.domain.model.Notification ignored
            ) {
                return ProviderResponse.accepted("SendGrid", "Accepted");
            }
        };
    }
}
