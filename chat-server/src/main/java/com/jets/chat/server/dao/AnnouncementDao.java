package com.jets.chat.server.dao;

import com.jets.chat.server.entity.Announcement;

import java.util.List;
import java.util.Optional;

public interface AnnouncementDao {
    Announcement save(Announcement announcement);
    List<Announcement> findRecent(int limit);
    List<Announcement> findAll();
    Optional<Announcement> findById(long id);
    boolean update(Announcement announcement);
    boolean deleteById(long id);
}
