package com.jets.chat.server.entity;

import java.sql.Timestamp;

public class Announcement {
    private long announcementId;
    private String content;
    private Timestamp sentAt;
    private String fontColor;
    private String fontStyle;
    private boolean isBold;
    private boolean isItalic;

    public Announcement() {
    }

    public Announcement(final long announcementId,
                        final String content,
                        final Timestamp sentAt,
                        final String fontColor,
                        final String fontStyle,
                        final boolean isBold,
                        final boolean isItalic) {
        this.announcementId = announcementId;
        this.content = content;
        this.sentAt = sentAt;
        this.fontColor = fontColor;
        this.fontStyle = fontStyle;
        this.isBold = isBold;
        this.isItalic = isItalic;
    }

    public long getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(final long announcementId) {
        this.announcementId = announcementId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(final Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(final String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(final String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(final boolean bold) {
        isBold = bold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setItalic(final boolean italic) {
        isItalic = italic;
    }
}
