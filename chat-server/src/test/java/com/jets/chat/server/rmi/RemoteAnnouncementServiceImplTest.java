package com.jets.chat.server.rmi;

import com.jets.chat.common.dto.AnnouncementDTO;
import com.jets.chat.server.service.AnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoteAnnouncementServiceImplTest {

    @Mock
    private AnnouncementService announcementService;

    @InjectMocks
    private RemoteAnnouncementServiceImpl remoteAnnouncementService;

    private List<AnnouncementDTO> mockAnnouncements;

    @BeforeEach
    void setUp() throws RemoteException {
        // Create mock announcements
        AnnouncementDTO dto1 = new AnnouncementDTO(1L, "Announcement 1",
                Timestamp.valueOf(LocalDateTime.now().minusHours(2)), "#000000", "Normal", true,
                false);

        AnnouncementDTO dto2 = new AnnouncementDTO(2L, "Announcement 2",
                Timestamp.valueOf(LocalDateTime.now().minusHours(1)), "#FFFFFF", "Italic", false,
                true);

        mockAnnouncements = Arrays.asList(dto1, dto2);

        // Create the remote service instance manually since constructor throws
        // RemoteException
        remoteAnnouncementService = new RemoteAnnouncementServiceImpl(announcementService);
    }

    @Test
    void testGetAllAnnouncements_Delegation() throws RemoteException {
        // Arrange
        when(announcementService.getAllAnnouncements()).thenReturn(mockAnnouncements);

        // Act
        List<AnnouncementDTO> result = remoteAnnouncementService.getAllAnnouncements();

        // Assert
        verify(announcementService, times(1)).getAllAnnouncements();
        assertNotNull(result);
        assertEquals(mockAnnouncements, result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAllAnnouncements_EmptyList() throws RemoteException {
        // Arrange
        when(announcementService.getAllAnnouncements()).thenReturn(Collections.emptyList());

        // Act
        List<AnnouncementDTO> result = remoteAnnouncementService.getAllAnnouncements();

        // Assert
        verify(announcementService, times(1)).getAllAnnouncements();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLatestAnnouncements_Delegation() throws RemoteException {
        // Arrange
        int fetchSize = 10;
        when(announcementService.getLatestAnnouncements(fetchSize)).thenReturn(mockAnnouncements);

        // Act
        List<AnnouncementDTO> result = remoteAnnouncementService.getLatestAnnouncements(fetchSize);

        // Assert
        verify(announcementService, times(1)).getLatestAnnouncements(fetchSize);
        assertNotNull(result);
        assertEquals(mockAnnouncements, result);
    }

    @Test
    void testGetLatestAnnouncements_WithDifferentFetchSize() throws RemoteException {
        // Arrange
        int fetchSize = 5;
        List<AnnouncementDTO> singleAnnouncement = mockAnnouncements.subList(0, 1);
        when(announcementService.getLatestAnnouncements(fetchSize)).thenReturn(singleAnnouncement);

        // Act
        List<AnnouncementDTO> result = remoteAnnouncementService.getLatestAnnouncements(fetchSize);

        // Assert
        verify(announcementService, times(1)).getLatestAnnouncements(fetchSize);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(singleAnnouncement, result);
    }

    @Test
    void testRemoteExceptionHandling() throws RemoteException {
        // This test verifies that RemoteException can be thrown (as declared in interface)
        // Arrange
        when(announcementService.getAllAnnouncements()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        // The remote service should propagate runtime exceptions
        assertThrows(RuntimeException.class, () -> {
            remoteAnnouncementService.getAllAnnouncements();
        });
    }
}