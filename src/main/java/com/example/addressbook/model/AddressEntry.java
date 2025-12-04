package com.example.addressbook.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
public class AddressEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(min = 2, max = 100)
  private String name;
  
  @Column(name = "full_name")
  @Size(min = 2, max = 100)
  private String fullName;
  
  // Getter that prioritizes fullName over name
  public String getDisplayName() {
    return fullName != null ? fullName : name;
  }
  
  // Setter that updates both fields during transition
  public void setDisplayName(String displayName) {
    this.name = displayName;
    this.fullName = displayName;
  }
  @NotBlank
  @Pattern(regexp = "^[0-9+\\-\\s()]+$")
  private String phone;
  
  @NotBlank
  @Email
  private String email;
  
  @NotBlank
  @Size(max = 200)
  private String street;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id")
  private AppUser user;
}
