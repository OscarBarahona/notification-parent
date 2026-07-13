package com.challenge.notifications.infrastructure.configuration;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Optional;


@EqualsAndHashCode
@ToString
public final class NotificationConfiguration {

    private final Optional<EmailConfiguration> email;
    private final Optional<SmsConfiguration> sms;
    private final Optional<PushConfiguration> push;

    private NotificationConfiguration(Builder builder) {
        this.email = Optional.ofNullable(builder.email);
        this.sms = Optional.ofNullable(builder.sms);
        this.push = Optional.ofNullable(builder.push);

        if (email.isEmpty() && sms.isEmpty() && push.isEmpty()) {
            throw new ConfigurationException(
                    ErrorCode.CONFIGURATION_ERROR,
                    "At least one notification channel must be configured"
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<EmailConfiguration> email() {
        return email;
    }

    public Optional<SmsConfiguration> sms() {
        return sms;
    }

    public Optional<PushConfiguration> push() {
        return push;
    }

    public static final class Builder {

        private EmailConfiguration email;
        private SmsConfiguration sms;
        private PushConfiguration push;

        private Builder() {
        }

        public Builder email(EmailConfiguration email) {
            this.email = email;
            return this;
        }

        public Builder sms(SmsConfiguration sms) {
            this.sms = sms;
            return this;
        }

        public Builder push(PushConfiguration push) {
            this.push = push;
            return this;
        }

        public NotificationConfiguration build() {
            return new NotificationConfiguration(this);
        }
    }
}
