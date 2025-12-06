package com.example.addressbook.event;

import com.example.addressbook.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddressEventHandler {
  private final SearchService searchService;

  @EventListener
  public void handleAddressCreated(AddressCreatedEvent event) {
    try {
      searchService.indexAddress(event.getAddress());
    } catch (Exception e) {
      // Log error but don't fail the operation
    }
  }

  @EventListener
  public void handleAddressDeleted(AddressDeletedEvent event) {
    try {
      searchService.deleteFromIndex(event.getAddressId());
    } catch (Exception e) {
      // Log error but don't fail the operation
    }
  }
}