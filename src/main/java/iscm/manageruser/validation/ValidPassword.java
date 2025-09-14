package iscm.manageruser.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "La contraseña no cumple con los requisitos de seguridad.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}