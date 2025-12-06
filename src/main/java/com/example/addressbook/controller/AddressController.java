package com.example.addressbook.controller;

import com.example.addressbook.dto.AddressRequestDTO;
import com.example.addressbook.model.AddressEntry;
import com.example.addressbook.service.AddressService;
import com.example.addressbook.service.SearchService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AddressController {
  private final AddressService service;
  
  @Autowired(required = false)
  private SearchService searchService;

  public AddressController(AddressService service) {
    this.service = service;
  }

  @GetMapping("/api/addresses")
  public List<AddressEntry> listAll() {
    return service.listAll();
  }

  @GetMapping("/api/users/{userId}/addresses")
  public List<AddressEntry> list(@PathVariable("userId") Long userId) {
    return service.listForUser(userId);
  }

  @PostMapping("/api/users/{userId}/addresses")
  public ResponseEntity<AddressEntry> create(
      @PathVariable("userId") Long userId, @Valid @RequestBody AddressRequestDTO entry) {
    AddressEntry saved = service.createForUser(userId, entry);
    return ResponseEntity.ok(saved);
  }

  @DeleteMapping("/api/users/{userId}/addresses/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable("userId") Long userId, @PathVariable("id") Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/api/addresses/search")
  public ResponseEntity<List<AddressEntry>> search(@RequestParam("q") String q) {
    if (searchService == null) {
      return ResponseEntity.badRequest().build();
    }
    try {
      List<AddressEntry> results = searchService.search(q);
      return ResponseEntity.ok(results);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
