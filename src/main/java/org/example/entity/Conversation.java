package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 
 * </p>
 *
 * @author elaina
 * @since 2025-03-26
 */
@Getter
@Setter
@ToString
@NoArgsConstructor  // 添加无参构造函数
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("conversation_id")
    private String conversationId;

    /**
     * 0:private, 1:group
     */
    private Byte conversationType;

    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastMessageTime;

    private String lastMessage;

    /**
     * 0:pending, 1:active
     */
    private Byte status;

    String avatarUrl;
    public Conversation(String conversationId,LocalDateTime createdAt)
    {
        this.conversationId = conversationId;
        this.createdAt = createdAt;
    }
}
