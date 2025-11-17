package com.example.addressbook.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.addressbook.mapper.AddressMapper;
import com.example.addressbook.model.AddressEntry;
import com.example.addressbook.model.AppUser;
import com.example.addressbook.repository.AddressEntryRepository;
import com.example.addressbook.repository.AppUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AddressServiceTest {
  @Mock private AddressEntryRepository addressRepo;

  @Mock private AppUserRepository appUserRepo;

  @InjectMocks private AddressService service;

  private AddressMapper mapper;

  private AppUser user;
  private AddressEntry entry1;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mapper = new AddressMapper();
    user = new AppUser();
    user.setId(1L);
    user.setUsername("user1");

    entry1 = new AddressEntry();
    entry1.setId(1L);
    entry1.setName("Alice");
    entry1.setUser(user);
  }

  @Test
  void testListForUser() {
    when(addressRepo.findByUserId(1L)).thenReturn(Arrays.asList(entry1));
    List<AddressEntry> result = service.listForUser(1L);
    assertEquals(1, result.size());
    assertEquals("Alice", result.get(0).getName());
  }

  @Test
  void testCreateForUser() {
    when(appUserRepo.findById(1L)).thenReturn(Optional.of(user));
    when(addressRepo.save(any(AddressEntry.class))).thenAnswer(i -> i.getArgument(0));

    AddressEntry newEntry = new AddressEntry();
    newEntry.setName("Bob");

    AddressEntry saved = service.createForUser(1L, mapper.toDTO(newEntry));
    assertNotNull(saved);
    assertEquals("Bob", saved.getName());
    assertEquals(user, saved.getUser());
  }

  @Test
  void testDelete() {
    doNothing().when(addressRepo).deleteById(1L);
    service.delete(1L);
    verify(addressRepo, times(1)).deleteById(1L);
  }

  @Test
  void testListAll() {
    AddressEntry entry2 = new AddressEntry();
    entry2.setId(2L);
    entry2.setName("Bob");

    when(addressRepo.findAll()).thenReturn(Arrays.asList(entry1, entry2));
    List<AddressEntry> result = service.listAll();
    assertEquals(2, result.size());
    assertEquals("Alice", result.get(0).getName());
    assertEquals("Bob", result.get(1).getName());
  }
}
