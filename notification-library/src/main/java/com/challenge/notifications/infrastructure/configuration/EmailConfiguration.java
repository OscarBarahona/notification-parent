package com.challenge.notifications.infrastructure.configuration;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;
import java.util.Objects;


@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public final class EmailConfiguration {

    public enum Provider {
        SENDGRID,
        MAILGUN
    }

    @ToString.Include
    private final Provider provider;

    private final String apiKey;

    @ToString.Include
    private final URI endpoint;

    @ToString.Include
    private final EmailAddress sender;

    private EmailConfiguration(Builder builder) {
        this.provider = Objects.requireNonNull(
                builder.provider,
                "provider cannot be null"
        );
        this.apiKey = requireText(builder.apiKey, "Email API key cannot be null or blank");
        this.endpoint = requireHttpEndpoint(
                builder.endpoint == null ? defaultEndpoint(provider) : builder.endpoint,
                "Email provider endpoint"
        );
        this.sender = Objects.requireNonNull(
                builder.sender,
                "sender cannot be null"
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public Provider provider() {
        return provider;
    }

    public String apiKey() {
        return apiKey;
    }

    public URI endpoint() {
        return endpoint;
    }

    public EmailAddress sender() {
        return sender;
    }

    private static URI defaultEndpoint(Provider provider) {
        return switch (provider) {
            case SENDGRID -> URI.create("https://api.sendgrid.com/v3/mail/send");
            case MAILGUN -> URI.create("https://api.mailgun.net/v3/messages");
        };
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw invalid(message);
        }
        return value.trim();
    }

    private static URI requireHttpEndpoint(URI endpoint, String fieldName) {
        if (endpoint == null
                || endpoint.getScheme() == null
                || endpoint.getHost() == null
                || !("http".equalsIgnoreCase(endpoint.getScheme())
                || "https".equalsIgnoreCase(endpoint.getScheme()))) {
            throw invalid(fieldName + " must be an absolute HTTP or HTTPS URI");
        }
        return endpoint;
    }

    private static ConfigurationException invalid(String message) {
        return new ConfigurationException(ErrorCode.CONFIGURATION_ERROR, message);
    }

    public static final class Builder {

        private Provider provider;
        private String apiKey;
        private URI endpoint;
        private EmailAddress sender;

        private Builder() {
        }

        public Builder provider(Provider provider) {
            this.provider = provider;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder endpoint(URI endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder sender(EmailAddress sender) {
            this.sender = sender;
            return this;
        }

        public EmailConfiguration build() {
            try {
                return new EmailConfiguration(this);
            } catch (NullPointerException exception) {
                throw invalid(exception.getMessage());
            }
        }
    }
}