package com.challenge.notifications.mapper;

import com.challenge.notifications.dto.NotificationRequest;
import com.challenge.notifications.dto.NotificationResponse;
import com.challenge.notifications.domain.enums.ErrorCode;
import com.challenge.notifications.domain.enums.NotificationChannel;
import com.challenge.notifications.domain.exception.ValidationException;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.Notification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.model.PushNotification;
import com.challenge.notifications.domain.model.SmsNotification;
import com.challenge.notifications.domain.valueobject.DeviceToken;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.PhoneNumber;
import com.challenge.notifications.domain.valueobject.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.Optional;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface NotificationRestMapper {

    default Notification toDomain(NotificationRequest request) {
        if (request == null) {
            throw invalid(ErrorCode.INVALID_NOTIFICATION, "Notification request cannot be null");
        }
        if (request.channel() == null) {
            throw invalid(ErrorCode.INVALID_NOTIFICATION, "Notification channel cannot be null");
        }

        MessageBody message = new MessageBody(request.message());

        return switch (request.channel()) {
            case EMAIL -> EmailNotification.create(
                    new EmailAddress(request.recipient()),
                    new Subject(request.subject()),
                    message
            );
            case SMS -> {
                rejectSubject(request.subject(), NotificationChannel.SMS);
                yield SmsNotification.create(
                        new PhoneNumber(request.recipient()),
                        message
                );
            }
            case PUSH -> {
                rejectSubject(request.subject(), NotificationChannel.PUSH);
                yield PushNotification.create(
                        new DeviceToken(request.recipient()),
                        message
                );
            }
        };
    }

    @Mapping(target = "notificationId", source = "notificationId.value")
    @Mapping(target = "errorCode", source = "errorCode")
    NotificationResponse toResponse(NotificationResult result);

    default String mapErrorCode(Optional<ErrorCode> errorCode) {
        return errorCode == null
                ? null
                : errorCode.map(ErrorCode::name).orElse(null);
    }

    private static void rejectSubject(String subject, NotificationChannel channel) {
        if (subject != null && !subject.isBlank()) {
            throw invalid(
                    ErrorCode.INVALID_SUBJECT,
                    "Subject is only supported for EMAIL notifications; channel was " + channel
            );
        }
    }

    private static ValidationException invalid(ErrorCode code, String message) {
        return new ValidationException(code, message);
    }
}
