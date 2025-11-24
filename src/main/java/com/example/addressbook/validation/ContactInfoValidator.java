package com.example.addressbook.validation;

import com.example.addressbook.dto.AddressRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactInfoValidator implements ConstraintValidator<ContactInfoValid, AddressRequestDTO> {

    @Override
    public boolean isValid(AddressRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;
        
        boolean hasPhone = dto.phone() != null && !dto.phone().trim().isEmpty();
        boolean hasEmail = dto.email() != null && !dto.email().trim().isEmpty();
        
        return hasPhone || hasEmail;
    }
}