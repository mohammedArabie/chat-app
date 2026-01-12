package com.jets.chat.server.service;

import com.jets.chat.common.dto.AnnouncementDTO;

import java.util.List;

public interface AnnouncementService {
    List<AnnouncementDTO> getAllAnnouncements();

    List<AnnouncementDTO> getLatestAnnouncements(int fetchSize);

    AnnouncementDTO createAnnouncement(AnnouncementDTO announcementDTO);
}
