package issuetracker.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final JavaMailSender mail;
    private final String from;

    public NotificationConsumer(JavaMailSender mail, @Value("${app.mail.from}") String from) {
        this.mail = mail;
        this.from = from;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void handle(IssueEvent event) {
        if (event.recipientEmail() != null) {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(from);
            m.setTo(event.recipientEmail());
            m.setSubject("[" + event.issueCode() + "] " + event.message());
            m.setText("Issue " + event.issueCode() + ": " + event.title()
                    + "\nStatus: " + event.status()
                    + "\n\n" + event.message());
            try {
                mail.send(m);
                log.info("Email sent to {} for {}", event.recipientEmail(), event.issueCode());
            } catch (RuntimeException e) {
                log.warn("Email to {} failed: {}", event.recipientEmail(), e.getMessage());
            }
        }
        if (event.recipientPhone() != null) {
            // Stub: a real provider (e.g. Twilio) would be called here.
            log.info("[SMS stub] to {}: {} {}", event.recipientPhone(),
                    event.issueCode(), event.message());
        }
    }
}