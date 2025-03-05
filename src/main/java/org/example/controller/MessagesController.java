package org.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.entity.Messages;
import org.example.service.impl.MessagesServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  // 改为 @RestController
@RequestMapping("/messages")
public class MessagesController {
    @Autowired
    private MessagesServiceImpl messagesService;

    @GetMapping("/conversation/{conversationId}")
    @ResponseBody
    public List<Messages> getConversationMessages(@PathVariable String conversationId) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getConversationId, conversationId)
                .orderByAsc(Messages::getCreatedAt);
        return messagesService.list(wrapper);
    }

    @GetMapping("/page/{current}/{size}")
    @ResponseBody
    public IPage<Messages> getMessagesByPage(
            @PathVariable Long current,
            @PathVariable Long size,
            @RequestParam String conversationId) {
        return messagesService.getMessagesByConversationId(current, size, conversationId);
    }

    @GetMapping("/sender/{senderId}")
    @ResponseBody
    public List<Messages> getSenderMessages(@PathVariable Long senderId) {
        LambdaQueryWrapper<Messages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Messages::getSenderId, senderId)
                .orderByDesc(Messages::getCreatedAt);
        return messagesService.list(wrapper);
    }
}