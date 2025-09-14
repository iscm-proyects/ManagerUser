package iscm.manageruser.service;

import iscm.manageruser.model.UserEntity;
import iscm.manageruser.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    // @Transactional(readOnly = true) es una buena práctica para métodos de solo lectura.
    // Mantiene la sesión de Hibernate abierta durante todo el método,
    // aunque con JOIN FETCH no es estrictamente necesario, es una buena costumbre.
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Usamos el método optimizado que trae los roles y devuelve un Optional
        UserEntity userEntity = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario '" + username + "' no existe."));

        // 2. Mapeamos los roles a las autoridades de Spring Security. Esto ahora es seguro.
        Collection<? extends GrantedAuthority> authorities = userEntity.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_".concat(role.getName().name())))
                .collect(Collectors.toSet());

        // 3. Mapeamos el estado de nuestro UserEntity a los flags de Spring Security
        boolean accountNonExpired = true; // Por defecto, o puedes añadir una lógica de fecha de expiración de cuenta
        boolean credentialsNonExpired = isCredentialsNonExpired(userEntity.getFecha_caducidad_password());
        boolean enabled = true; // Podrías tener un campo 'enabled' en tu UserEntity
        boolean accountNonLocked = !userEntity.isBloqueado(); // ¡Importante!

        // 4. Creamos y devolvemos el objeto UserDetails con la lógica correcta
        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(), // La contraseña ya debe estar hasheada en la BD
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities
        );
    }

    // Método auxiliar para mantener el código limpio
    private boolean isCredentialsNonExpired(LocalDate expirationDate) {
        if (expirationDate == null) {
            return true; // Si no hay fecha de caducidad, nunca expira
        }
        return LocalDate.now().isBefore(expirationDate);
    }
}