package com.example.addressbook.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AddressEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String phone;
  private String email;
  private String street;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private AppUser user;
}
