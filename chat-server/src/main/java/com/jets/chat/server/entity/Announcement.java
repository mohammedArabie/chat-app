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

    public Announcement(long announcementId,
                        String content,
                        Timestamp sentAt,
                        String fontColor,
                        String fontStyle,
                        boolean isBold,
                        boolean isItalic) {
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

    public void setAnnouncementId(long announcementId) {
        this.announcementId = announcementId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    public void setItalic(boolean italic) {
        isItalic = italic;
    }
}
