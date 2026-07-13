package com.challenge.notifications.application.facade;

import com.challenge.notifications.application.port.in.SendNotificationInputPort;
import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.application.usecase.SendAsyncNotificationUseCase;
import com.challenge.notifications.application.usecase.SendNotificationUseCase;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.NotificationResult;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;


public final class NotificationFacade {

    private final SendNotificationInputPort synchronousUseCase;
    private final SendAsyncNotificationUseCase asynchronousUseCase;

    public NotificationFacade(
            SendNotificationInputPort synchronousUseCase,
            SendAsyncNotificationUseCase asynchronousUseCase
    ) {
        this.synchronousUseCase = Objects.requireNonNull(
                synchronousUseCase,
                "synchronousUseCase cannot be null"
        );
        this.asynchronousUseCase = Objects.requireNonNull(
                asynchronousUseCase,
                "asynchronousUseCase cannot be null"
        );
    }

    public static NotificationFacade create(
            Collection<? extends NotificationProviderPort> providers
    ) {
        return create(providers, ForkJoinPool.commonPool());
    }

    public static NotificationFacade create(
            Collection<? extends NotificationProviderPort> providers,
            Executor executor
    ) {
        SendNotificationUseCase synchronous = new SendNotificationUseCase(providers);
        SendAsyncNotificationUseCase asynchronous =
                new SendAsyncNotificationUseCase(synchronous, executor);
        return new NotificationFacade(synchronous, asynchronous);
    }

    public NotificationResult send(Notification notification) {
        return synchronousUseCase.send(notification);
    }

    public CompletableFuture<NotificationResult> sendAsync(Notification notification) {
        return asynchronousUseCase.sendAsync(notification);
    }
}
