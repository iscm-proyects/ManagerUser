package iscm.manageruser.service;

import iscm.manageruser.exception.BadRequestException;
import iscm.manageruser.exception.ResourceNotFoundException;
import iscm.manageruser.mapper.UserMapper;
import iscm.manageruser.model.ERole;
import iscm.manageruser.model.OldPassword;
import iscm.manageruser.model.RoleEntity;
import iscm.manageruser.model.UserEntity;
import iscm.manageruser.repositories.RoleRepository;
import iscm.manageruser.repositories.UserRepository;
import iscm.manageruser.request.CreateUserDTO;
import iscm.manageruser.request.UpdateAccountDTO;
import iscm.manageruser.request.UpdatePasswordDTO;
import iscm.manageruser.request.UserResponseDTO;
import iscm.manageruser.utils.GenerateAlphaNumericString;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // Inyección de dependencias por constructor
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(CreateUserDTO createUserDTO) {
        if (userRepository.findByUsername(createUserDTO.getUsername()).isPresent() || userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new BadRequestException("El nombre de usuario o el email ya están registrados.");
        }

        Set<RoleEntity> roles = findAndValidateRoles(createUserDTO.getRoles());

        UserEntity userEntity = UserEntity.builder()
                .username(createUserDTO.getUsername())
                .password(passwordEncoder.encode(createUserDTO.getPassword()))
                .email(createUserDTO.getEmail())
                .primer_nombre(createUserDTO.getPrimer_nombre())
                .segundo_nombre(createUserDTO.getSegundo_nombre())
                .apellido_paterno(createUserDTO.getApellido_paterno())
                .apellido_materno(createUserDTO.getApellido_materno())
                .punto(createUserDTO.getPunto())
                .ciudad(createUserDTO.getCiudad())
                .cargo(createUserDTO.getCargo())
                .fecha_caducidad_password(LocalDate.now().plusDays(90)) // Contraseña inicial válida por 90 días
                .intentos_ingreso(0)
                .bloqueado(false)
                .roles(roles)
                .build();

        try {
            UserEntity savedUser = userRepository.save(userEntity);
            return userMapper.toUserResponseDTO(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Error de datos: es posible que el nombre de usuario o email ya existan.");
        }
    }

    @Override
    @Transactional
    public void unlockUser(String username) {
        UserEntity user = findUserByUsername(username);
        user.setBloqueado(false);
        user.setIntentos_ingreso(0);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        // Usar el método con JOIN FETCH para evitar N+1
        return userRepository.findAllWithRoles().stream()
                .map(userMapper::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        UserEntity user = findUserByUsernameWithRoles(username);
        return userMapper.toUserResponseDTO(user);
    }

    @Override
    @Transactional
    public void updatePassword(String username, UpdatePasswordDTO dto) {
        UserEntity user = findUserByUsernameWithOldPasswords(username);

        if (!passwordEncoder.matches(dto.getPasswordActual(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("La nueva contraseña no puede ser igual a la actual.");
        }
        if (isPasswordInHistory(dto.getNewPassword(), user.getOld_passwords())) {
            throw new BadRequestException("La nueva contraseña no puede ser una de las contraseñas utilizadas anteriormente.");
        }

        // La validación de complejidad ya se hizo con @ValidPassword en el DTO.

        archiveOldPassword(user);
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setFecha_caducidad_password(LocalDate.now().plusDays(90));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String resetPassword(String username) {
        UserEntity user = findUserByUsernameWithOldPasswords(username);
        String newPassword = GenerateAlphaNumericString.getRandomString(14);

        archiveOldPassword(user);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFecha_caducidad_password(LocalDate.now().plusDays(1)); // Forzar cambio al día siguiente
        userRepository.save(user);

        return newPassword; // El controlador decide qué hacer con esto
    }

    @Override
    @Transactional
    public UserResponseDTO updateAccount(String username, UpdateAccountDTO dto) {
        UserEntity user = findUserByUsername(username);
        Set<RoleEntity> roles = findAndValidateRoles(dto.getRoles());

        user.setPunto(dto.getPunto());
        user.setCiudad(dto.getCiudad());
        user.setCargo(dto.getCargo());
        user.setRoles(roles);

        UserEntity updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDTO(updatedUser);
    }

    // --- Métodos de ayuda privados ---

    private UserEntity findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
    }

    private UserEntity findUserByUsernameWithRoles(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
    }

    private UserEntity findUserByUsernameWithOldPasswords(String username) {
        // Asumiendo que has creado este método en el repositorio para eficiencia
        // Si no, la carga LAZY funcionará dentro del método transaccional
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con username: " + username));
    }

    private Set<RoleEntity> findAndValidateRoles(Set<String> roleNames) {
        return roleNames.stream()
                // 1. Busca el rol por su nombre.
                .map(roleName -> roleRepository.findByName(ERole.valueOf(roleName.toUpperCase()))
                        // 2. Si no lo encuentra, lanza una excepción clara.
                        .orElseThrow(() -> new BadRequestException("El rol no existe: " + roleName)))
                // 3. El resultado es un Set de RoleEntity "manejadas" (managed) por JPA.
                .collect(Collectors.toSet());
    }

    private boolean isPasswordInHistory(String newPassword, Set<OldPassword> oldPasswords) {
        return oldPasswords.stream()
                .anyMatch(old -> passwordEncoder.matches(newPassword, old.getPassword()));
    }

    private void archiveOldPassword(UserEntity user) {
        OldPassword oldPassword = OldPassword.builder()
                .password(user.getPassword())
                .build();
        user.getOld_passwords().add(oldPassword);
    }
    private Set<RoleEntity> findOrCreateRoles(Set<String> roleNames) {
        Set<RoleEntity> roles = new HashSet<>();
        for (String roleNameStr : roleNames) {
            ERole roleName = ERole.valueOf(roleNameStr.toUpperCase());

            // Intenta encontrar el rol en la base de datos
            RoleEntity role = roleRepository.findByName(roleName)
                    .orElse(null); // Obtén el rol o null si no existe

            if (role == null) {
                // El rol no existe, créalo.
                // Gracias a CascadeType.PERSIST, este nuevo objeto será guardado junto con el usuario.
                role = RoleEntity.builder().name(roleName).build();
            }

            roles.add(role);
        }
        return roles;
    }
}