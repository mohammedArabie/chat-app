package com.jets.chat.server.entity;

public class ChatGroup {
    private long chatId; // Same as Chat.chatId
    private String groupName;
    private long ownerId; // User who created the group

    public ChatGroup() {
    }

    public ChatGroup(long chatId, String groupName, long ownerId) {
        this.chatId = chatId;
        this.groupName = groupName;
        this.ownerId = ownerId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
