package iscm.manageruser.repositories;

import iscm.manageruser.model.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    // Optimizado: Devuelve Optional para manejar de forma segura el caso "no encontrado"
    Optional<UserEntity> findByUsername(String username);

    // Optimizado: Devuelve Optional y tiene el tipo correcto (UserEntity)
    Optional<UserEntity> findByEmail(String email);

    // NUEVO MÉTODO DE ALTO RENDIMIENTO:
    // Resuelve el problema N+1 al traer usuarios y sus roles en una sola consulta.
    // Úsalo en las pantallas o procesos donde necesites mostrar los roles.
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles")
    List<UserEntity> findAllWithRoles();
    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserEntity> findByUsernameWithRoles(String username);

}
