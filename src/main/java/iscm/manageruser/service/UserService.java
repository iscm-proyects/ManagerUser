package iscm.manageruser.service;

import iscm.manageruser.request.CreateUserDTO;
import iscm.manageruser.request.UpdateAccountDTO;
import iscm.manageruser.request.UpdatePasswordDTO;
import iscm.manageruser.request.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(CreateUserDTO createUserDTO);
    void unlockUser(String username);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserByUsername(String username);
    void updatePassword(String username, UpdatePasswordDTO updatePasswordDTO);
    String resetPassword(String username);
    UserResponseDTO updateAccount(String username, UpdateAccountDTO updateAccountDTO);
}