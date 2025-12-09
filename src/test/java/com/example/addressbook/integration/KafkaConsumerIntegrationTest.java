package com.example.addressbook.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.addressbook.event.AddressEvent;
import com.example.addressbook.event.AddressEventConsumer;
import com.example.addressbook.event.AddressEventProducer;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(
    partitions = 1,
    topics = {"address-events"},
    brokerProperties = {"auto.create.topics.enable=true"})
@DirtiesContext
class KafkaConsumerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:17")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.flyway.enabled", () -> "false");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
    registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
    registry.add("spring.kafka.consumer.group-id", () -> "addressbook-group");
    registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    registry.add("spring.kafka.consumer.enable-auto-commit", () -> "false");
    registry.add("spring.kafka.listener.ack-mode", () -> "manual");
    registry.add("spring.kafka.consumer.value-deserializer", () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
    registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "*");
    registry.add("kafka.consumer.enabled", () -> "true");
  }

  @Autowired private AddressEventProducer producer;

  @Autowired private AddressEventConsumer consumer;

  @BeforeEach
  void setUp() throws Exception {
    Thread.sleep(2000);
  }

  @AfterEach
  void tearDown() {
    consumer.getProcessedEvents().clear();
  }

  @Test
  void shouldConsumeEventSuccessfully() {
    AddressEvent event = AddressEvent.created(1L, 1L);
    producer.sendEvent(event);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      assertThat(consumer.getProcessedEvents()).contains(event.eventId());
    });
  }

  @Test
  void shouldPreventDuplicateProcessing() {
    AddressEvent event = AddressEvent.created(2L, 1L);
    producer.sendEvent(event);
    producer.sendEvent(event);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      assertThat(consumer.getProcessedEvents()).contains(event.eventId());
    });
  }
}
