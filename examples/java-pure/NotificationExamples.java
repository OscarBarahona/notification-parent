package examples;

import com.challenge.notifications.application.facade.NotificationFacade;
import com.challenge.notifications.application.port.out.NotificationProviderPort;
import com.challenge.notifications.domain.model.EmailNotification;
import com.challenge.notifications.domain.model.NotificationResult;
import com.challenge.notifications.domain.valueobject.EmailAddress;
import com.challenge.notifications.domain.valueobject.MessageBody;
import com.challenge.notifications.domain.valueobject.Subject;
import com.challenge.notifications.infrastructure.configuration.EmailConfiguration;
import com.challenge.notifications.infrastructure.configuration.NotificationConfiguration;
import com.challenge.notifications.infrastructure.factory.NotificationProviderFactory;

import java.util.List;

public final class NotificationExamples {

    private NotificationExamples() {
    }

    public static void main(String[] args) {
        EmailConfiguration email = EmailConfiguration.builder()
                .provider(EmailConfiguration.Provider.SENDGRID)
                .apiKey(requiredEnvironmentVariable("SENDGRID_API_KEY"))
                .sender(new EmailAddress("sender@example.com"))
                .build();

        NotificationConfiguration configuration = NotificationConfiguration.builder()
                .email(email)
                .build();

        List<NotificationProviderPort> providers =
                NotificationProviderFactory.create(configuration);

        NotificationFacade facade = NotificationFacade.create(providers);

        EmailNotification notification = EmailNotification.create(
                new EmailAddress("customer@example.com"),
                new Subject("Welcome"),
                new MessageBody("Your account has been created")
        );

        NotificationResult result = facade.send(notification);
        System.out.println(result);
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required");
        }
        return value;
    }
}
