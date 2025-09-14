package iscm.manageruser.repositories;

import iscm.manageruser.model.ERole;
import iscm.manageruser.model.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface RoleRepository extends CrudRepository<RoleEntity, Long> {

    //@Query("Select r from RoleEntity r where r.name=:name")
    RoleEntity findFirstByName(ERole name);
    /**
     * Busca un rol por su nombre enum.
     * Spring Data JPA genera automáticamente la consulta "SELECT r FROM RoleEntity r WHERE r.name = ?1"
     * a partir del nombre de este método.
     *
     * @param name El enum del rol a buscar (ej. ERole.ADMIN).
     * @return un Optional que contiene el RoleEntity si se encuentra, o un Optional vacío si no.
     */
    Optional<RoleEntity> findByName(ERole name);
}
