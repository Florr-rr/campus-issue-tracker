package issuetracker.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "issues.events";
    public static final String NOTIFY_QUEUE = "issues.notifications";

    @Bean
    public TopicExchange issuesExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(NOTIFY_QUEUE, true);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange issuesExchange) {
        return BindingBuilder.bind(notificationsQueue).to(issuesExchange).with("issue.#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}