package issuetracker.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbit;

    public EventPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    /** Never lets a broker problem break the ticket operation itself. */
    public void publish(IssueEvent event) {
        try {
            rabbit.convertAndSend(RabbitConfig.EXCHANGE,
                    "issue." + event.type().toLowerCase(), event);
        } catch (RuntimeException e) {
            log.warn("Could not publish {} event for issue {}: {}",
                    event.type(), event.issueId(), e.getMessage());
        }
    }
}