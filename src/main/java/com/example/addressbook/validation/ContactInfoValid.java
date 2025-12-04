package com.example.addressbook.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContactInfoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContactInfoValid {
    String message() default "Either phone or email must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}