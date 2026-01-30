package com.jets.chat.server.service;

import com.jets.chat.common.dto.RegisterRequestDTO;
import com.jets.chat.common.dto.RegisterResponseDTO;
public interface UserService {

    public RegisterResponseDTO register(RegisterRequestDTO dto);
    public byte[] getProfilePicture(String picturePath);

}
