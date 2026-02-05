package com.jets.chat.server.util;

import com.jets.chat.common.dto.UserDTO;
import com.jets.chat.common.enums.UserStatus;
import com.jets.chat.server.entity.User;

public class DtoMapper {
    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(user.getUserId(), user.getDisplayName(), UserStatus.OFFLINE,
                user.getEmail(), user.getPhoneNumber(), user.getPicturePath());
    }
}
