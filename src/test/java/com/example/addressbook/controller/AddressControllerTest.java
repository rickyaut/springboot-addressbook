package com.example.addressbook.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.addressbook.model.AddressEntry;
import com.example.addressbook.service.AddressService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AddressController.class)
public class AddressControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private AddressService service;

  private AddressEntry entry1;
  private AddressEntry entry2;

  @BeforeEach
  void setUp() {
    entry1 = new AddressEntry();
    entry1.setId(1L);
    entry1.setName("Alice");

    entry2 = new AddressEntry();
    entry2.setId(2L);
    entry2.setName("Bob");
  }

  @Test
  void testGetUserAddresses() throws Exception {
    List<AddressEntry> all = Arrays.asList(entry1, entry2);
    when(service.listForUser(Mockito.anyLong())).thenReturn(all);

    mockMvc
        .perform(get("/api/users/1/addresses"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Alice")))
        .andExpect(content().string(containsString("Bob")));
  }
}
