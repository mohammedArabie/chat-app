package com.jets.chat.common.dto;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnnouncementDTOTest {

    @Test
    void testSerializationCheck() {
        // Verify that the class implements Serializable (crucial for RMI)
        AnnouncementDTO dto = new AnnouncementDTO();
        assertTrue(dto instanceof Serializable,
                "AnnouncementDTO must implement Serializable for RMI compatibility");
    }

    @Test
    void testConstructorAndGetters() {
        // Arrange
        long expectedId = 1L;
        String expectedContent = "Test Announcement";
        Timestamp expectedSentAt = Timestamp.valueOf(LocalDateTime.now());
        String expectedFontColor = "#FF0000";
        String expectedFontStyle = "Arial";
        boolean expectedBold = true;
        boolean expectedItalic = false;

        // Act
        AnnouncementDTO dto = new AnnouncementDTO(expectedId, expectedContent, expectedSentAt,
                expectedFontColor, expectedFontStyle, expectedBold, expectedItalic);

        // Assert
        assertEquals(expectedId, dto.getAnnouncementId());
        assertEquals(expectedContent, dto.getContent());
        assertEquals(expectedSentAt, dto.getSentAt());
        assertEquals(expectedFontColor, dto.getFontColor());
        assertEquals(expectedFontStyle, dto.getFontStyle());
        assertEquals(expectedBold, dto.isBold());
        assertEquals(expectedItalic, dto.isItalic());
    }

    @Test
    void testSetters() {
        // Arrange
        AnnouncementDTO dto = new AnnouncementDTO();
        long expectedId = 2L;
        String expectedContent = "Another Announcement";
        Timestamp expectedSentAt = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        String expectedFontColor = "#00FF00";
        String expectedFontStyle = "Verdana";
        boolean expectedBold = false;
        boolean expectedItalic = true;

        // Act
        dto.setAnnouncementId(expectedId);
        dto.setContent(expectedContent);
        dto.setSentAt(expectedSentAt);
        dto.setFontColor(expectedFontColor);
        dto.setFontStyle(expectedFontStyle);
        dto.setBold(expectedBold);
        dto.setItalic(expectedItalic);

        // Assert
        assertEquals(expectedId, dto.getAnnouncementId());
        assertEquals(expectedContent, dto.getContent());
        assertEquals(expectedSentAt, dto.getSentAt());
        assertEquals(expectedFontColor, dto.getFontColor());
        assertEquals(expectedFontStyle, dto.getFontStyle());
        assertEquals(expectedBold, dto.isBold());
        assertEquals(expectedItalic, dto.isItalic());
    }

    // Note: AnnouncementDTO doesn't override equals/hashCode, so we skip those
    // tests
}