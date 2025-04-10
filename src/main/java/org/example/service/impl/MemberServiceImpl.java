package org.example.service.impl;

import org.example.entity.Member;
import org.example.mapper.MemberMapper;
import org.example.service.IMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author elaina
 * @since 2025-03-26
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService {
    @Override
    public Member getMember(String conversationId, Long userId) {
        return this.baseMapper.getMember(conversationId, userId);
    }
    @Override
    public List<Member> getMemberByUserId(Long userId) {
        return this.baseMapper.getMemberByUserId(userId);
    }
    @Override
    public List<Member> getMemberByConversationId(String conversationId){
        return this.baseMapper.getMembersByConversationId(conversationId);
    }
}
