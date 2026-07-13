FROM gradle:8.13-jdk21-alpine AS builder

WORKDIR /workspace
COPY --chown=gradle:gradle . .

USER gradle
RUN gradle clean check :notification-demo-api:bootJar \
    --no-daemon \
    --no-configuration-cache

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

WORKDIR /app
COPY --from=builder \
    /workspace/notification-demo-api/build/libs/notification-demo-api.jar \
    /app/notification-demo-api.jar

ENV PORT=8080
EXPOSE 8080

USER app

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD curl --fail --silent "http://localhost:${PORT}/actuator/health" || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/notification-demo-api.jar"]
