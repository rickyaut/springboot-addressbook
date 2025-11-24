package com.example.addressbook.dto;

import com.example.addressbook.validation.ContactInfoValid;
import jakarta.validation.constraints.*;

@ContactInfoValid
public record AddressRequestDTO(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone format")
    String phone,
    @Email(message = "Invalid email format")
    String email,
    @NotBlank(message = "Street is required")
    @Size(max = 200, message = "Street must not exceed 200 characters")
    String street
) {}
