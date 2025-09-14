package iscm.manageruser.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iscm.manageruser.request.CreateUserDTO;
import iscm.manageruser.request.UpdateAccountDTO;
import iscm.manageruser.request.UpdatePasswordDTO;
import iscm.manageruser.request.UserResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import iscm.manageruser.service.UserService;

@RestController
@RequestMapping("/api/v1")
@Tag
        (
                name = "User Management",
                description = "API para la gestión completa de usuarios (CRUD, seguridad y acciones administrativas)."
        )
@SecurityRequirement(name = "bearerAuth")
public class ManageController {

    private final UserService userService;

    public ManageController(UserService userService) {
        this.userService = userService;
    }

    // --- Endpoints Administrativos ---

    @Operation(
            summary = "Crear un nuevo usuario",
            description = "Crea un nuevo usuario en el sistema con la información y roles proporcionados. Requiere rol de ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos (ej. email/username duplicado, contraseña débil).", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado. El usuario autenticado no tiene el rol 'ADMIN'.", content = @Content)
    })
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserResponseDTO newUser = userService.createUser(createUserDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newUser.getId()).toUri();
        return ResponseEntity.created(location).body(newUser);
    }

    @Operation
            (
                    summary = "Obtener una lista de todos los usuarios",
                    description = "Devuelve una lista paginada y filtrable de todos los usuarios del sistema. Requiere rol de ADMIN."
            )
    @ApiResponse
            (
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente."
            )
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation
            (
                    summary = "Obtener un usuario por su nombre de usuario",
                    description = "Recupera los detalles de un usuario específico. Un ADMIN puede ver a cualquier usuario, mientras que un usuario normal solo puede ver su propia información.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado y devuelto.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "El usuario con el nombre de usuario especificado no fue encontrado.", content = @Content)
    })
    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @Parameter(description = "Nombre de usuario del usuario a buscar.", required = true, example = "jperez") @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @Operation
            (
                    summary = "Actualizar la cuenta de un usuario",
                    description = "Permite a un ADMIN actualizar la información editable (punto, ciudad, cargo, roles) de una cuenta de usuario."
            )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta actualizada exitosamente.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de solicitud inválidos.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.", content = @Content)
    })
    @PutMapping("/users/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateAccount(
            @Parameter(description = "Nombre de usuario del usuario a actualizar.", required = true) @PathVariable String username,
            @Valid @RequestBody UpdateAccountDTO updateAccountDTO) {
        UserResponseDTO updatedUser = userService.updateAccount(username, updateAccountDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation
            (
                    summary = "Desbloquear una cuenta de usuario",
                    description = "Permite a un ADMIN desbloquear una cuenta que ha sido bloqueada por exceso de intentos de login fallidos."
            )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario desbloqueado exitosamente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.", content = @Content)
    })
    @PostMapping("/users/{username}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unlockUser(
            @Parameter(description = "Nombre de usuario del usuario a desbloquear.", required = true) @PathVariable String username) {
        userService.unlockUser(username);
        return ResponseEntity.noContent().build();
    }

    @Operation
            (
                    summary = "Resetear la contraseña de un usuario",
                    description = "Un ADMIN puede forzar el reseteo de la contraseña de un usuario. Se genera una nueva contraseña temporal y se devuelve en la respuesta."
            )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña reseteada. La nueva contraseña está en el cuerpo de la respuesta.", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"newPassword\": \"aBcDeFgHiJkL12\"}"))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.", content = @Content)
    })
    @PostMapping("/users/{username}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Parameter(description = "Nombre de usuario cuya contraseña será reseteada.", required = true) @PathVariable String username) {
        String newPassword = userService.resetPassword(username);
        Map<String, String> response = Map.of("newPassword", newPassword);
        return ResponseEntity.ok(response);
    }

    // --- Endpoints para el Usuario Autenticado ---

    @Tag(name = "Account Management", description = "Endpoints para que el usuario gestione su propia cuenta.")
    @Operation
            (
                    summary = "Actualizar mi propia contraseña",
                    description = "Permite al usuario autenticado cambiar su propia contraseña. Debe proporcionar la contraseña actual y la nueva."
            )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contraseña actualizada exitosamente.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. la contraseña actual no coincide, la nueva es débil o ya fue usada).", content = @Content)
    })
    @PostMapping("/me/update-password")
    public ResponseEntity<Void> updateMyPassword(
            @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO,
            Authentication authentication) {
        String username = authentication.getName();
        userService.updatePassword(username, updatePasswordDTO);
        return ResponseEntity.noContent().build();
    }
}