package com.jets.chat.server.service;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.server.dao.AnnouncementDao;
import com.jets.chat.server.entity.Announcement;
import com.jets.chat.server.service.impl.AnnouncementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceImplTest {

    @Mock
    private AnnouncementDao announcementDao;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    @Captor
    private ArgumentCaptor<Announcement> announcementCaptor;

    private Announcement announcement1;
    private Announcement announcement2;
    private AnnouncementDTO announcementDTO;

    @BeforeEach
    void setUp() {
        // Create test entities
        announcement1 = new Announcement();
        announcement1.setAnnouncementId(1L);
        announcement1.setContent("First Announcement");
        announcement1.setSentAt(Timestamp.valueOf(LocalDateTime.now().minusHours(2)));
        announcement1.setFontColor("#000000");
        announcement1.setFontStyle("Normal");
        announcement1.setBold(true);
        announcement1.setItalic(false);

        announcement2 = new Announcement();
        announcement2.setAnnouncementId(2L);
        announcement2.setContent("Second Announcement");
        announcement2.setSentAt(Timestamp.valueOf(LocalDateTime.now().minusHours(1)));
        announcement2.setFontColor("#FFFFFF");
        announcement2.setFontStyle("Italic");
        announcement2.setBold(false);
        announcement2.setItalic(true);

        // Create test DTO
        announcementDTO = new AnnouncementDTO();
        announcementDTO.setContent("New Announcement");
        announcementDTO.setFontColor("#FF0000");
        announcementDTO.setFontStyle("Bold");
        announcementDTO.setBold(true);
        announcementDTO.setItalic(false);
    }

    @Test
    void testGetAllAnnouncements_MappingFlow() {
        // Arrange
        List<Announcement> mockEntities = Arrays.asList(announcement1, announcement2);
        when(announcementDao.findAll()).thenReturn(mockEntities);

        // Act
        List<AnnouncementDTO> result = announcementService.getAllAnnouncements();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first announcement mapping
        AnnouncementDTO firstDto = result.get(0);
        assertEquals(announcement1.getAnnouncementId(), firstDto.getAnnouncementId());
        assertEquals(announcement1.getContent(), firstDto.getContent());
        assertEquals(announcement1.isBold(), firstDto.isBold());
        assertEquals(announcement1.isItalic(), firstDto.isItalic());
        assertEquals(announcement1.getFontStyle(), firstDto.getFontStyle());
        assertEquals(announcement1.getFontColor(), firstDto.getFontColor());
        assertEquals(announcement1.getSentAt(), firstDto.getSentAt());

        // Verify second announcement mapping
        AnnouncementDTO secondDto = result.get(1);
        assertEquals(announcement2.getAnnouncementId(), secondDto.getAnnouncementId());
        assertEquals(announcement2.getContent(), secondDto.getContent());
        assertEquals(announcement2.isBold(), secondDto.isBold());
        assertEquals(announcement2.isItalic(), secondDto.isItalic());
        assertEquals(announcement2.getFontStyle(), secondDto.getFontStyle());
        assertEquals(announcement2.getFontColor(), secondDto.getFontColor());
        assertEquals(announcement2.getSentAt(), secondDto.getSentAt());

        verify(announcementDao, times(1)).findAll();
    }

    @Test
    void testGetLatestAnnouncements_Flow() {
        // Arrange
        List<Announcement> mockEntities = Arrays.asList(announcement1);
        when(announcementDao.findRecent(5)).thenReturn(mockEntities);

        // Act
        List<AnnouncementDTO> result = announcementService.getLatestAnnouncements(5);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(announcementDao, times(1)).findRecent(5);
        verify(announcementDao, never()).findAll();

        // Verify the argument passed to findRecent
        verify(announcementDao).findRecent(5);
    }

    @Test
    void testCreateAnnouncement_IDReturnLogic() {
        // Arrange
        when(announcementDao.save(any(Announcement.class))).thenAnswer(invocation -> {
            Announcement announcement = invocation.getArgument(0);
            // Simulate database setting ID and timestamp
            announcement.setAnnouncementId(100L);
            announcement.setSentAt(Timestamp.valueOf(LocalDateTime.now()));
            return announcement;
        });

        // Act
        AnnouncementDTO result = announcementService.createAnnouncement(announcementDTO);

        // Assert
        verify(announcementDao, times(1)).save(announcementCaptor.capture());

        // Verify the entity passed to DAO
        Announcement capturedAnnouncement = announcementCaptor.getValue();
        assertEquals(announcementDTO.getContent(), capturedAnnouncement.getContent());
        assertEquals(announcementDTO.isBold(), capturedAnnouncement.isBold());
        assertEquals(announcementDTO.isItalic(), capturedAnnouncement.isItalic());
        assertEquals(announcementDTO.getFontColor(), capturedAnnouncement.getFontColor());
        assertEquals(announcementDTO.getFontStyle(), capturedAnnouncement.getFontStyle());

        // Verify the returned DTO
        assertEquals(100L, result.getAnnouncementId());
        assertNotNull(result.getSentAt());
        assertEquals(announcementDTO.getContent(), result.getContent());
        assertEquals(announcementDTO.isBold(), result.isBold());
        assertEquals(announcementDTO.isItalic(), result.isItalic());
        assertEquals(announcementDTO.getFontColor(), result.getFontColor());
        assertEquals(announcementDTO.getFontStyle(), result.getFontStyle());
    }

    @Test
    void testCreateAnnouncement_NullInput() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            announcementService.createAnnouncement(null);
        });
    }
}