package iscm.manageruser.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    // Regex para validar la contraseña:
    // ^                 - inicio de la cadena
    // (?=.*[0-9])       - al menos un dígito
    // (?=.*[a-z])       - al menos una letra minúscula
    // (?=.*[A-Z])       - al menos una letra mayúscula
    // (?=.*[!@#&()–[{}]:;',?/*~$^+=<>]) - al menos un carácter especial
    // .                 - cualquier carácter
    // {14,}             - al menos 14 caracteres de longitud
    // $                 - fin de la cadena
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{14,}$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}