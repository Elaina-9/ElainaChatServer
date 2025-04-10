package org.example.service.impl;

import org.example.entity.Conversation;
import org.example.mapper.ConversationMapper;
import org.example.service.IConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author elaina
 * @since 2025-03-26
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements IConversationService {

}
