package com.app.login.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka event publisher for login events
 * Only active when Kafka is enabled
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class LoginEventPublisher {

    private final KafkaTemplate<String, LoginEvent> kafkaTemplate;
    private static final String TOPIC = "login-events";

    @Autowired
    public LoginEventPublisher(KafkaTemplate<String, LoginEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish login event to Kafka
     */
    public void publishLoginEvent(LoginEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.getUsername(), event);
            log.info("Published login event for user: {} with event type: {}", 
                    event.getUsername(), event.getEventType());
        } catch (Exception e) {
            log.error("Failed to publish login event for user: {}", event.getUsername(), e);
        }
    }
}
