package com.example.addressbook.service;

import com.example.addressbook.dto.AddressRequestDTO;
import com.example.addressbook.event.AddressEvent;
import com.example.addressbook.event.AddressEventProducer;
import com.example.addressbook.mapper.AddressMapper;
import com.example.addressbook.model.AddressEntry;
import com.example.addressbook.model.AppUser;
import com.example.addressbook.repository.AddressEntryRepository;
import com.example.addressbook.repository.AppUserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {
  private final AddressEntryRepository addressRepo;
  private final AppUserRepository appUserRepo;
  private final AddressEventProducer eventProducer;
  private final AddressMapper mapper = new AddressMapper();

  public List<AddressEntry> listForUser(Long userId) {
    return addressRepo.findByUserId(userId);
  }

  public AddressEntry createForUser(Long userId, AddressRequestDTO dto) {
    AppUser user =
        appUserRepo
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    AddressEntry entry = mapper.toEntity(dto);
    entry.setUser(user);

    AddressEntry saved = addressRepo.save(entry);
    eventProducer.sendEvent(AddressEvent.created(saved.getId(), userId));
    return saved;
  }

  public void delete(Long id) {
    AddressEntry entry =
        addressRepo
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Address not found"));
    addressRepo.deleteById(id);
    eventProducer.sendEvent(AddressEvent.deleted(id, entry.getUser().getId()));
  }

  public List<AddressEntry> listAll() {
    return addressRepo.findAll();
  }
}
