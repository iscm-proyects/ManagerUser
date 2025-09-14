package iscm.manageruser.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) para encapsular las credenciales de inicio de sesión.
 * Usar un DTO en lugar de la entidad UserEntity en el endpoint de login es una
 * práctica de seguridad crucial para prevenir la sobreexposición de datos y
 * vulnerabilidades de deserialización.
 *
 * Se utiliza un 'record' de Java para una definición concisa e inmutable.
 *
 * @param username El nombre de usuario proporcionado para la autenticación.
 * @param password La contraseña en texto plano proporcionada para la autenticación.
 */
public record LoginRequest(
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        String username,

        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
}