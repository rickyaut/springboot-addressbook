package com.example.addressbook.event;

import java.util.UUID;

public record AddressEvent(String eventId, String eventType, Long addressId, Long userId) {
  public static AddressEvent created(Long addressId, Long userId) {
    return new AddressEvent(UUID.randomUUID().toString(), "ADDRESS_CREATED", addressId, userId);
  }

  public static AddressEvent deleted(Long addressId, Long userId) {
    return new AddressEvent(UUID.randomUUID().toString(), "ADDRESS_DELETED", addressId, userId);
  }
}
