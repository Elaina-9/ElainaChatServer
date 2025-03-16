package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 
 * </p>
 *
 * @author elaina
 * @since 2025-03-15
 */
@Getter
@Setter
@ToString
public class Friends implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long friendId;

    /**
     * 0:pending, 1:accepted, 2:blocked
     */
    private Byte status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Friends(Long userId, Long friendId){
        this.userId = userId;
        this.friendId = friendId;
        this.status = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
