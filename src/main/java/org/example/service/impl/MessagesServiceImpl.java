package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.entity.Messages;
import org.example.mapper.MessagesMapper;
import org.example.service.IMessagesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author elaina
 * @since 2025-02-28
 */
@Service
public class MessagesServiceImpl extends ServiceImpl<MessagesMapper, Messages> implements IMessagesService {
    @Override
    public IPage<Messages> getMessagesByConversationId(Long lastMessageId,Long current, Long size, String conversationId) {
        Page<Messages> page = new Page<>(current, size);
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        //根据ConversationId获得senderId和receiverId
        Long senderId = Long.parseLong(conversationId.split("_")[0]);
        Long receiverId = Long.parseLong(conversationId.split("_")[1]);
        wrapper.nested(w -> w
                .eq(Messages::getSenderId, receiverId)
                .eq(Messages::getReceiverId, senderId)
                .or()
                .eq(Messages::getSenderId, senderId)
                .eq(Messages::getReceiverId, receiverId))
                .lt(Messages::getId, lastMessageId)
                .orderByDesc(Messages::getId)
                .orderByDesc(Messages::getCreatedAt);
        return this.page(page, wrapper);
    }
    @Override
    @Transactional
    public boolean addMessage(Messages message) {
        return this.save(message);
    }

}
