package com.jets.chat.server.service.impl;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.common.rmi.AnnouncementCallback;
import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.entity.Announcement;
import com.jets.chat.server.service.AnnouncementService;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnnouncementServiceImpl implements AnnouncementService {
    private final AnnouncementDao announcementDao;
    private final Map<String, AnnouncementCallback> callbacks = new ConcurrentHashMap<>();

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

        List<String> deadSessions = new ArrayList<>();
        callbacks.forEach((sessionId, callback) -> {
            try {
                callback.onAnnouncementReceived(announcementDTO);
            } catch (RemoteException e) {
                deadSessions.add(sessionId);
            }
        });
        deadSessions.forEach(callbacks::remove);
        return announcementDTO;
    }

    @Override
    public void registerCallback(String sessionId, AnnouncementCallback callback)
            throws RemoteException {
        callbacks.put(sessionId, callback);
    }

    @Override
    public void unregisterCallback(String sessionId) throws RemoteException {
        callbacks.remove(sessionId);
    }

    @Override
    public int getActiveCallbackCount() {
        return callbacks.size();
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
    // ADD THIS METHOD:
    @Override
    public void clearCallbacks() {
        int count = callbacks.size();
        callbacks.clear();
        System.out.println("✓ Cleared " + count + " announcement callbacks");
    }
}
