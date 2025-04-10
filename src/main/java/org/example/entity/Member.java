package org.example.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@TableName("member")
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "conversation_id", type = IdType.NONE)
    private String conversationId;

    private Long userId;

    private String nickname;

    /**
     * 0:member, 1:admin, 2:owner
     */
    private Byte role;

    private LocalDateTime joinedAt;

    private Long lastReadId;

    private Boolean isMuted;
}