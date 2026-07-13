package com.challenge.notifications.infrastructure.configuration;

import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.exception.ConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;
import java.util.Objects;


@EqualsAndHashCode
@ToString(onlyExplicitlyIncluded = true)
public final class PushConfiguration {

    public enum Provider {
        FIREBASE
    }

    @ToString.Include
    private final Provider provider;

    @ToString.Include
    private final String projectId;

    private final String credentials;

    @ToString.Include
    private final URI endpoint;

    private PushConfiguration(Builder builder) {
        this.provider = Objects.requireNonNull(
                builder.provider,
                "provider cannot be null"
        );
        this.projectId = requireText(
                builder.projectId,
                "Push project id cannot be null or blank"
        );
        this.credentials = requireText(
                builder.credentials,
                "Push credentials cannot be null or blank"
        );
        this.endpoint = requireHttpEndpoint(
                builder.endpoint == null
                        ? URI.create("https://fcm.googleapis.com/v1/projects")
                        : builder.endpoint,
                "Push provider endpoint"
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public Provider provider() {
        return provider;
    }

    public String projectId() {
        return projectId;
    }

    public String credentials() {
        return credentials;
    }

    public URI endpoint() {
        return endpoint;
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
        private String projectId;
        private String credentials;
        private URI endpoint;

        private Builder() {
        }

        public Builder provider(Provider provider) {
            this.provider = provider;
            return this;
        }

        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder credentials(String credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder endpoint(URI endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public PushConfiguration build() {
            try {
                return new PushConfiguration(this);
            } catch (NullPointerException exception) {
                throw invalid(exception.getMessage());
            }
        }
    }
}
