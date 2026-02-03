package com.jets.chat.server.service.impl;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.entity.Announcement;
import com.jets.chat.server.context.ServerManager;
import com.jets.chat.server.service.AnnouncementService;

import java.rmi.RemoteException;
import java.util.List;

public class AnnouncementServiceImpl implements AnnouncementService {
    private final AnnouncementDao announcementDao;

    public AnnouncementServiceImpl(AnnouncementDao announcementDao) {
        this.announcementDao = announcementDao;
    }

    @Override
    public List<AnnouncementDTO> getAllAnnouncements() {
        return announcementDao.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<AnnouncementDTO> getLatestAnnouncements(int fetchSize) {
        return announcementDao.findRecent(fetchSize).stream().map(this::convertToDto).toList();
    }

    @Override
    public AnnouncementDTO createAnnouncement(AnnouncementDTO announcementDTO) {
        Announcement announcement = new Announcement();

        announcement.setContent(announcementDTO.getContent());
        announcement.setBold(announcementDTO.isBold());
        announcement.setItalic(announcementDTO.isItalic());
        announcement.setFontColor(announcementDTO.getFontColor());
        announcement.setFontStyle(announcementDTO.getFontStyle());

        announcementDao.save(announcement);

        announcementDTO.setSentAt(announcement.getSentAt());
        announcementDTO.setAnnouncementId(announcement.getAnnouncementId());

        // Send to all online users using the existing online clients from ServerManager
        broadcastToOnlineUsers(announcementDTO);

        return announcementDTO;
    }

    private void broadcastToOnlineUsers(AnnouncementDTO announcementDTO) {
        // Get the online clients from ServerManager (same as used in UserServiceImpl)
        var onlineClients = ServerManager.getInstance().getOnlineClients();

        onlineClients.forEach((userId, clientCallback) -> {
            try {
                clientCallback.onAnnouncementReceived(announcementDTO);
            } catch (RemoteException e) {
                System.err.println(
                        "Failed to send announcement to user " + userId + ": " + e.getMessage());
            }
        });
    }

    @Override
    public int getActiveCallbackCount() {
        // Return the number of online users from ServerManager
        return ServerManager.getInstance().getOnlineClients().size();
    }

    private AnnouncementDTO convertToDto(Announcement entity) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setAnnouncementId(entity.getAnnouncementId());
        dto.setContent(entity.getContent());
        dto.setBold(entity.isBold());
        dto.setItalic(entity.isItalic());
        dto.setFontStyle(entity.getFontStyle());
        dto.setFontColor(entity.getFontColor());
        dto.setSentAt(entity.getSentAt());
        return dto;
    }
}