package iscm.manageruser.repositories;

import iscm.manageruser.model.OldPassword;
import org.springframework.data.repository.CrudRepository;

public interface OldPasswordRepository extends CrudRepository<OldPassword, Long> {
}
