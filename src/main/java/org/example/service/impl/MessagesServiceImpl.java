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

import java.time.LocalDateTime;
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
    public List<Messages> getNewMessagesByConversationId(String conversationId, Long messageId) {
            LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Messages::getConversationId, conversationId)
                    .gt(Messages::getId, messageId)
                    .orderByAsc(Messages::getId);
            return this.list(wrapper);
    }
    @Override
    public IPage<Messages> getOldMessagesByConversationId(String conversationId, Long messageId) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getConversationId, conversationId)
                .lt(Messages::getId, messageId)
                .orderByDesc(Messages::getId);
        return this.page(new Page<>(1, 5), wrapper);
    }
    @Override
    @Transactional
    public Messages addMessage(Messages message) {
        this.save(message);
        return message;
    }

}
