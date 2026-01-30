package com.jets.chat.server.service;

import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;

import java.util.Optional;

public interface UserService {

    Optional<UserDTO> findUserById(long id);

    boolean updateUser(UserDTO userDTO);

    boolean updateUserPassword(long id, String passwordHash);

    boolean updateUserStatus(long userId, UserStatus status);

    UserStatus getUserStatus(long userId);

}
