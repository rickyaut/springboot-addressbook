package com.example.addressbook.service;

import com.example.addressbook.dto.AddressRequestDTO;
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

    return addressRepo.save(entry);
  }

  public void delete(Long id) {
    addressRepo.deleteById(id);
  }

  public List<AddressEntry> listAll() {
    return addressRepo.findAll();
  }
}
