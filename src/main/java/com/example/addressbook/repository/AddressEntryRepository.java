package com.example.addressbook.repository;

import com.example.addressbook.model.AddressEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressEntryRepository extends JpaRepository<AddressEntry, Long> {
  List<AddressEntry> findByUserId(Long userId);
}
