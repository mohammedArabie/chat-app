package com.jets.chat.server.dao;

import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    User save(User user);

    Optional<User> findById(long id);

    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> getAllUsers();

    Optional<User> findByEmail(String email);

    boolean update(User user);

    boolean updatePassword(long id, String passwordHash);

    boolean updateStatus(long userId, UserStatus status);

    UserStatus getStatus(long userId);

    boolean createSession(String sessionId, long userId);

    boolean deleteSession(String sessionId);

    boolean isSessionValid(long userId, String sessionId);
}
