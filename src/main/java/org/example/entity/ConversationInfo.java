package org.example.entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ConversationInfo {
    String ConversationId;
    String name;
    String avatar;
    String lastMessage;
    LocalDateTime lastMessageTime;
    int unreadCount;
    Long userId; //私聊时为对方id，群聊时为null
    Byte status; // 0:pending, 1:active

    public ConversationInfo(String conversationId, String name, String avatar, String lastMessage, LocalDateTime lastMessageTime, int unreadCount, Long userId, Byte status) {
        ConversationId = conversationId;
        this.name = name;
        this.avatar = avatar;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
        this.userId = userId;
        this.status = status;
    }

}
