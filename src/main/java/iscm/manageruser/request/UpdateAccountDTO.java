package iscm.manageruser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "DTO para actualizar la información de una cuenta de usuario existente. Solo los campos aquí presentes pueden ser modificados.")
public class UpdateAccountDTO {

    @Schema(description = "Nuevo punto o agencia del usuario.", example = "Central", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 20)
    private String sucursal;

    @Schema(description = "Nueva ciudad del usuario.", example = "Santa Cruz", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 20)
    private String ciudad;

    @Schema(description = "Nuevo cargo del usuario.", example = "Jefe De Contabilidad", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 45)
    private String cargo;

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

    @Schema
            (
                    description = "Conjunto completo de los nuevos roles para el usuario. Reemplazará los roles existentes.",
                    example = "[\"JEFE\", \"CONTABILIDAD\"]",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
    @NotEmpty
    private Set<String> roles;
}