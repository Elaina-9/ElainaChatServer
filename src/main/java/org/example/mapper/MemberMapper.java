package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.entity.Member;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.time.LocalDateTime;
import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author elaina
 * @since 2025-03-26
 */
public interface MemberMapper extends BaseMapper<Member> {
    Member getMember(@Param("conversationId") String conversationId,
                                  @Param("userId") Long userId);
    List<Member> getMemberByUserId(Long userId);
    List<Member> getMembersByConversationId(String conversationId);
}

