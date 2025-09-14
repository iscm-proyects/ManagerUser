package iscm.manageruser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "DTO para que un usuario actualice su propia contraseña.")
public class UpdatePasswordDTO {

    @Schema(description = "La contraseña actual del usuario, para verificación.", example = "Password.Anterior.123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String passwordActual;

    @Schema(description = "La nueva contraseña deseada. Debe cumplir con las políticas de seguridad.", example = "Password.Nueva.456!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String newPassword;
}