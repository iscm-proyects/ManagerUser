package iscm.manageruser.config;

import iscm.manageruser.model.ERole;
import iscm.manageruser.model.RoleEntity;
import iscm.manageruser.model.UserEntity;
import iscm.manageruser.repositories.RoleRepository;
import iscm.manageruser.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional // ¡MUY IMPORTANTE! Envuelve toda la operación en una única transacción.
    public void run(String... args) {
        // --- 1. Sincronizar los roles en la base de datos ---
        logger.info("Sincronizando roles de la base de datos...");
        for (ERole roleName : EnumSet.allOf(ERole.class)) {
            // findByName devuelve Optional<RoleEntity>. Si está vacío, lo creamos.
            roleRepository.findByName(roleName).orElseGet(() -> {
                logger.info("Creando rol que no existe: {}", roleName);
                return roleRepository.save(RoleEntity.builder().name(roleName).build());
            });
        }
        logger.info("Sincronización de roles completada.");

        // --- 2. Crear el Usuario Administrador si no existe ---
        final String adminUsername = "admin";
        if (userRepository.findByUsername(adminUsername).isPresent()) {
            logger.info("El usuario 'admin' ya existe. No se realizarán cambios.");
            return; // Salir si el admin ya existe
        }

        logger.info("Creando usuario administrador por defecto...");

        // --- Carga explícita de los roles que necesitamos desde la BD ---
        // Esto garantiza que son entidades "managed"
        RoleEntity adminRole = roleRepository.findByName(ERole.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Error crítico: El rol ADMIN no se pudo encontrar o crear."));

        RoleEntity sistemasRole = roleRepository.findByName(ERole.SISTEMAS)
                .orElseThrow(() -> new IllegalStateException("Error crítico: El rol SISTEMAS no se pudo encontrar o crear."));

        Set<RoleEntity> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminRoles.add(sistemasRole);

        UserEntity adminUser = UserEntity.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode("Bisa.2025")) // Usa una contraseña segura
                .email("admin@bisabolsa.com")
                .primer_nombre("Administrador")
                .apellido_paterno("del")
                .apellido_materno("Sistema")
                .punto("Oficina Central")
                .ciudad("La Paz")
                .cargo("Administrador de Sistemas")
                .fecha_caducidad_password(LocalDate.now().plusDays(90))
                .bloqueado(false)
                .intentos_ingreso(0)
                .roles(adminRoles) // Asigna el SET de roles "managed"
                .build();

        userRepository.save(adminUser);
        logger.info("Usuario 'admin' creado exitosamente con roles: {}", adminRoles.stream().map(r -> r.getName().name()).toList());
    }
}