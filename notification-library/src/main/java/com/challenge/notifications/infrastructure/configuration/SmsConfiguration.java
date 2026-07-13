package com.challenge.notifications.infrastructure.configuration;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;
import java.util.Objects;


@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public final class SmsConfiguration {

    public enum Provider {
        TWILIO
    }

    @ToString.Include
    private final Provider provider;

    @ToString.Include
    private final String accountSid;

    private final String authToken;

    @ToString.Include
    private final URI endpoint;

    @ToString.Include
    private final PhoneNumber fromNumber;

    private SmsConfiguration(Builder builder) {
        this.provider = Objects.requireNonNull(
                builder.provider,
                "provider cannot be null"
        );
        this.accountSid = requireText(
                builder.accountSid,
                "SMS account SID cannot be null or blank"
        );
        this.authToken = requireText(
                builder.authToken,
                "SMS authentication token cannot be null or blank"
        );
        this.endpoint = requireHttpEndpoint(
                builder.endpoint == null
                        ? URI.create("https://api.twilio.com/2010-04-01/Accounts")
                        : builder.endpoint,
                "SMS provider endpoint"
        );
        this.fromNumber = Objects.requireNonNull(
                builder.fromNumber,
                "fromNumber cannot be null"
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public Provider provider() {
        return provider;
    }

    public String accountSid() {
        return accountSid;
    }

    public String authToken() {
        return authToken;
    }

    public URI endpoint() {
        return endpoint;
    }

    public PhoneNumber fromNumber() {
        return fromNumber;
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
        private String accountSid;
        private String authToken;
        private URI endpoint;
        private PhoneNumber fromNumber;

        private Builder() {
        }

        public Builder provider(Provider provider) {
            this.provider = provider;
            return this;
        }

        public Builder accountSid(String accountSid) {
            this.accountSid = accountSid;
            return this;
        }

        public Builder authToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public Builder endpoint(URI endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder fromNumber(PhoneNumber fromNumber) {
            this.fromNumber = fromNumber;
            return this;
        }

        public SmsConfiguration build() {
            try {
                return new SmsConfiguration(this);
            } catch (NullPointerException exception) {
                throw invalid(exception.getMessage());
            }
        }
    }
}