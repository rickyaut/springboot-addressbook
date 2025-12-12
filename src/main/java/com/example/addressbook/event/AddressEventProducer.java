package com.example.addressbook.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AddressEventProducer {
    private final KafkaTemplate<String, AddressEvent> kafkaTemplate;

    public AddressEventProducer(
            @Autowired(required = false) KafkaTemplate<String, AddressEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(AddressEvent event) {
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate not available, skipping event: {}", event);
            return;
        }
        try {
            kafkaTemplate.send("address-events", event.eventId(), event).get();
            log.info("Sent event: {}", event);
        } catch (Exception e) {
            log.error("Failed to send event: {}", event, e);
            throw new RuntimeException("Failed to send Kafka event", e);
        }
    }
}
