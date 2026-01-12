package com.jets.chat.server.dto;

import java.time.format.DateTimeFormatter;

public class MessageResponseDto {
    private long messageId;
    private String senderName;
    private String content;
    private String formattedTimestamp;
    private String messageType;

    private String fontStyle;
    private String fontColor;
    private int fontSize;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private String backgroundColor;

    public MessageResponseDto() {
    }

    public MessageResponseDto(long messageId, String senderName, String content,
            String formattedTimestamp, String messageType, String fontStyle, String fontColor,
            int fontSize, boolean isBold, boolean isItalic, boolean isUnderline,
            String backgroundColor) {
        this.messageId = messageId;
        this.senderName = senderName;
        this.content = content;
        this.formattedTimestamp = formattedTimestamp;
        this.messageType = messageType;
        this.fontStyle = fontStyle;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderline = isUnderline;
        this.backgroundColor = backgroundColor;
    }

    public void setSentAtFromTimestamp(java.sql.Timestamp ts) {
        if (ts != null) {
            this.formattedTimestamp = ts.toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        }
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = formattedTimestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
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

    public boolean isUnderline() {
        return isUnderline;
    }

    public void setUnderline(boolean underline) {
        isUnderline = underline;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}