package com.ddbs.choroid_session_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotBlankIfPresentValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlankIfPresent {
    String message() default "must not be blank if present";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
