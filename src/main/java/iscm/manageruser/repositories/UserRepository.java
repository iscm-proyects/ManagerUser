package iscm.manageruser.repositories;

import iscm.manageruser.model.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    // Devuelve Optional para manejar de forma segura el caso "no encontrado"
    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.old_passwords WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithOldPasswords(String username);

    // Devuelve Optional y tiene el tipo correcto (UserEntity)
    Optional<UserEntity> findByEmail(String email);

    // NUEVO METODO DE ALTO RENDIMIENTO:
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles")
    List<UserEntity> findAllWithRoles();
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithRoles(String username);
}