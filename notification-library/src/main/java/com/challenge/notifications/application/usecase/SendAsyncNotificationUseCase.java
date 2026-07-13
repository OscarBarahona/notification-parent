package com.challenge.notifications.application.usecase;

import com.challenge.notifications.application.port.in.SendNotificationInputPort;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.NotificationResult;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public final class SendAsyncNotificationUseCase {

    private final SendNotificationInputPort synchronousUseCase;
    private final Executor executor;

    public SendAsyncNotificationUseCase(
            SendNotificationInputPort synchronousUseCase,
            Executor executor
    ) {
        this.synchronousUseCase = Objects.requireNonNull(
                synchronousUseCase,
                "synchronousUseCase cannot be null"
        );
        this.executor = Objects.requireNonNull(executor, "executor cannot be null");
    }

    public CompletableFuture<NotificationResult> sendAsync(Notification notification) {
        return CompletableFuture.supplyAsync(
                () -> synchronousUseCase.send(notification),
                executor
        );
    }
}
