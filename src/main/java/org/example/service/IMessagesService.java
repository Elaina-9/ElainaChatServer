package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.entity.Messages;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.Messages;
import java.util.List;
/**
 * <p>
 *  服务类
 * </p>
 *
 * @author elaina
 * @since 2025-02-28
 */
public interface IMessagesService extends IService<Messages> {
    IPage<Messages> getMessagesByConversationId(Long lastMessageId,Long current, Long size, String conversationId);
    boolean addMessage(Messages message);
}
