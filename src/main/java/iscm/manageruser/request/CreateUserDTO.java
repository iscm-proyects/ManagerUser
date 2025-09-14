package iscm.manageruser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema
        (description = "DTO para la creación de un nuevo usuario. Todos los campos marcados como obligatorios deben ser proporcionados.")
public class CreateUserDTO {

    @Schema
            (
                    description = "Nombre de usuario único en el sistema. No se podrá cambiar una vez creado.",
                    example = "jperez",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
    @NotBlank
    @Size(max = 30)
    private String username;

    @Schema
            (
                    description = "Contraseña inicial del usuario. Debe cumplir con las políticas de seguridad de la organización.",
                    example = "Password.Seguro.123!",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
    @NotBlank
    private String password;

    @Schema
            (
                    description = "Dirección de correo electrónico única del usuario.",
                    example = "juan.perez@email.com",
                    requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Email
    @Size(max = 80)
    private String email;

    @Schema(description = "Primer nombre del usuario.", example = "Juan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 25)
    private String primer_nombre;

    @Schema(description = "Segundo nombre del usuario (opcional).", example = "Carlos")
    @Size(max = 25)
    private String segundo_nombre;

    @Schema(description = "Apellido paterno del usuario.", example = "Perez", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 25)
    private String apellido_paterno;

    @Schema(description = "Apellido materno del usuario (opcional).", example = "Mamani")
    @Size(max = 25)
    private String apellido_materno;

    @Schema(description = "Punto o agencia a la que pertenece el usuario.", example = "Central", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 20)
    private String sucursal;


    @Schema(description = "Número de celular del usuario.", example = "710 12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 10)
    private String celular;

    @Schema(description = "Número de teléfono del usuario.", example = "2 2451234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 10)
    private String telefono;

    @Schema(description = "Dirección del usuario.", example = "Cale #3, N 123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 45)
    private String direccion;

    @Schema(description = "Ciudad donde se encuentra el punto o agencia.", example = "La Paz", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 20)
    private String ciudad;

    @Schema(description = "Cargo oficial del usuario dentro de la empresa.", example = "Atención al Cliente", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 45)
    private String cargo;

    @Schema
            (
                    description = "Conjunto de roles asignados al usuario. Deben ser valores válidos del enum ERole.",
                    example = "[\"JEFE\", \"CONTABILIDAD\"]",
                    requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private Set<String> roles;
}