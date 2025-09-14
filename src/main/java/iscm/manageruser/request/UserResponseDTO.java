package iscm.manageruser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "DTO que representa la vista pública de un usuario. No contiene información sensible como la contraseña.")
public class UserResponseDTO {

    @Schema(description = "ID único del usuario en la base de datos.", example = "1")
    private Long id;

    @Schema(description = "Nombre de usuario.", example = "jperez")
    private String username;

    @Schema(description = "Correo electrónico del usuario.", example = "juan.perez@iscm.com")
    private String email;

    @Schema(description = "Nombre completo del usuario, construido a partir de sus partes.", example = "Juan Carlos Perez Mamani")
    private String nombreCompleto;

    @Schema(description = "Punto o agencia del usuario.", example = "Central")
    private String sucursal;

    @Schema(description = "Número de teléfono fijo del usuario.", example = "2 243939")
    private String telefono;

    @Schema(description = "Dirección del usuario.", example = "Calle 123 # 12-12")
    private String direccion;

    @Schema(description = "Número de celular del usuario.",example = "710 12345")
    private String celular;

    @Schema(description = "Ciudad del usuario.", example = "La Paz")
    private String ciudad;

    @Schema(description = "Cargo del usuario.", example = "Atención al Cliente")
    private String cargo;

    @Schema(description = "Indica si la cuenta del usuario está bloqueada.", example = "false")
    private boolean bloqueado;

    @Schema(description = "Fecha en la que la contraseña actual del usuario expirará.", example = "2025-10-15")
    private LocalDate fechaCaducidadPassword;

    @Schema(description = "Conjunto de roles asignados al usuario.", example = "[\"OFICIAL\", \"OPERACIONES\"]")
    private Set<String> roles;
}