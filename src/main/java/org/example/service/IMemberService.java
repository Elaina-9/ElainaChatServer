package org.example.service;

import org.example.entity.Member;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author elaina
 * @since 2025-03-26
 */
public interface IMemberService extends IService<Member> {
    Member getMember(String conversationId, Long userId);
    List<Member> getMemberByUserId(Long userId);
    List<Member> getMemberByConversationId(String conversationId);
}
