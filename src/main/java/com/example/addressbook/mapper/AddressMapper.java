package com.example.addressbook.mapper;

import com.example.addressbook.dto.AddressRequestDTO;
import com.example.addressbook.model.AddressEntry;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
  public AddressEntry toEntity(AddressRequestDTO dto) {
    AddressEntry entry = new AddressEntry();
    entry.setName(dto.name());
    entry.setPhone(dto.phone());
    entry.setEmail(dto.email());
    entry.setStreet(dto.street());
    return entry;
  }

  public AddressRequestDTO toDTO(AddressEntry entry) {
    return new AddressRequestDTO(
        entry.getName(), entry.getPhone(), entry.getEmail(), entry.getStreet());
  }
}
