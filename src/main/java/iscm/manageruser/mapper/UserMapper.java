package iscm.manageruser.mapper;

import iscm.manageruser.model.UserEntity;
import iscm.manageruser.request.UserResponseDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponseDTO toUserResponseDTO(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }

        return new UserResponseDTO(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                buildNombreCompleto(userEntity),
                userEntity.getPunto(),
                userEntity.getCiudad(),
                userEntity.getCargo(),
                userEntity.isBloqueado(),
                userEntity.getFecha_caducidad_password(),
                userEntity.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }

    private String buildNombreCompleto(UserEntity userEntity) {
        StringBuilder nombreCompleto = new StringBuilder();
        nombreCompleto.append(userEntity.getPrimer_nombre());
        if (userEntity.getSegundo_nombre() != null && !userEntity.getSegundo_nombre().isEmpty()) {
            nombreCompleto.append(" ").append(userEntity.getSegundo_nombre());
        }
        if (userEntity.getApellido_paterno() != null && !userEntity.getApellido_paterno().isEmpty()) {
            nombreCompleto.append(" ").append(userEntity.getApellido_paterno());
        }
        if (userEntity.getApellido_materno() != null && !userEntity.getApellido_materno().isEmpty()) {
            nombreCompleto.append(" ").append(userEntity.getApellido_materno());
        }
        return nombreCompleto.toString();
    }
}