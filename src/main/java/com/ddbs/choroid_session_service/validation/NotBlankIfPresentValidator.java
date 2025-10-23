package com.ddbs.choroid_session_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotBlankIfPresentValidator implements ConstraintValidator<NotBlankIfPresent, String>{

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context)
    {
        //null is valid
        if (value == null) return true;
        //non-null must be non-blank
        return !value.isBlank();
    }
}
