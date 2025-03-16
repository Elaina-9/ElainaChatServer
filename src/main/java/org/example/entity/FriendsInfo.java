package org.example.entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class FriendsInfo {
    Long userId;
    String userName;
    String userAvatar;
    String lastMessage;
    LocalDateTime lastMessageTime;
}
