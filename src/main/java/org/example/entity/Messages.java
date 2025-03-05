package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * @since 2025-02-28
 */
@Getter
@Setter
@ToString
@NoArgsConstructor  // 添加无参构造函数
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String conversationId;

    private Long senderId;

    private Long receiverId;

    private String messageContent;

    private LocalDateTime createdAt;
    public Messages(Long senderId,Long receiverId,String messageContent){
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageContent = messageContent;
        this.createdAt = LocalDateTime.now();
    }
}
