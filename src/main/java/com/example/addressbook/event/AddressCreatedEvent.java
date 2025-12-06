package com.example.addressbook.event;

import com.example.addressbook.model.AddressEntry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AddressCreatedEvent {
  private final AddressEntry address;
}