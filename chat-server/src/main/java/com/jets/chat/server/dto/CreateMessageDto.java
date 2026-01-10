package com.jets.chat.server.dto;

public class CreateMessageDto {
    private long chatId;
    private long senderId;
    private String messageType;
    private String content;

    private String fontStyle = "Arial";
    private String fontColor = "#000000";
    private int fontSize = 12;
    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean isUnderline = false;
    private String backgroundColor = "transparent";

    public CreateMessageDto() {
    }

    public CreateMessageDto(long chatId, long senderId, String messageType, String content, String fontStyle, String fontColor, int fontSize, boolean isBold, boolean isItalic, boolean isUnderline, String backgroundColor) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.messageType = messageType;
        this.content = content;
        this.fontStyle = fontStyle;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderline = isUnderline;
        this.backgroundColor = backgroundColor;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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