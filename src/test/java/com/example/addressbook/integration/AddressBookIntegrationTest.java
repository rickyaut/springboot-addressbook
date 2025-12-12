package com.example.addressbook.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.addressbook.dto.AddressRequestDTO;
import com.example.addressbook.model.AppUser;
import com.example.addressbook.repository.AppUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
        properties = {
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
        })
@AutoConfigureMockMvc
@Testcontainers
class AddressBookIntegrationTest {

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
        registry.add("kafka.enabled", () -> "false");
    }

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private AppUserRepository userRepository;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        userRepository.save(user);
    }

    @Test
    void shouldCreateAndRetrieveAddress() throws Exception {
        AddressRequestDTO request =
                new AddressRequestDTO("John Doe", "0432123456", "john@example.com", "123 Main St");

        mockMvc.perform(
                        post("/api/users/1/addresses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("0432123456"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        mockMvc.perform(get("/api/users/1/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name=='John Doe')]").exists());
    }

    @Test
    void shouldDeleteAddress() throws Exception {
        AddressRequestDTO request =
                new AddressRequestDTO(
                        "Jane Smith", "0433456789", "jane@example.com", "456 Oak Ave");

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/users/1/addresses")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long addressId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/users/1/addresses/" + addressId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldValidateRequiredFields() throws Exception {
        AddressRequestDTO invalidRequest = new AddressRequestDTO("", "", "", "");

        mockMvc.perform(
                        post("/api/users/1/addresses")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListAllAddresses() throws Exception {
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
