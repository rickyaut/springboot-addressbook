package com.example.addressbook.event;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AddressEventConsumer {
  private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

  public Set<String> getProcessedEvents() {
    return processedEvents;
  }

  @KafkaListener(topics = "address-events", groupId = "addressbook-group", autoStartup = "${kafka.consumer.enabled:true}")
  public void consume(AddressEvent event, Acknowledgment ack) {
    if (processedEvents.contains(event.eventId())) {
      log.warn("Duplicate event detected: {}", event.eventId());
      ack.acknowledge();
      return;
    }

    try {
      log.info("Processing event: {}", event);
      processedEvents.add(event.eventId());
      ack.acknowledge();
    } catch (Exception e) {
      log.error("Failed to process event: {}", event, e);
    }
  }
}
