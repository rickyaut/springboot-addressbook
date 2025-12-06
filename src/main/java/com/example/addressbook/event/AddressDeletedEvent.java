package com.example.addressbook.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AddressDeletedEvent {
  private final Long addressId;
}