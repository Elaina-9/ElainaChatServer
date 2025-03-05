package org.example;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.google.gson.Gson;
import org.apache.catalina.User;
import org.example.entity.Content;
import org.example.service.IMessagesService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.example.entity.Messages;
import org.example.entity.ContentType;
import org.example.entity.Content;
import org.example.service.IUsersService;
import org.example.entity.Users;

public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    // 用户id和Channel的映射关系
    private static final Map<Long, Channel> userIdToChannelMap = new ConcurrentHashMap<>();
    private static final Map<Channel, Long> channelToUserIdMap = new ConcurrentHashMap<>();

    private IMessagesService messagesService;
    private IUsersService usersService;
    private Gson gson = new Gson();


    public NettyServerHandler() {
        // 通过 SpringUtils 获取 Service 实例
        this.messagesService = SpringUtils.getBean(IMessagesService.class);
        this.usersService = SpringUtils.getBean(IUsersService.class);
        // 创建支持 LocalDateTime 的 Gson 实例
        this.gson = CustomGson.getCustomGson();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        System.out.println("Client connected - " + channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Long userId = channelToUserIdMap.get(channel);

        if (userId != null) {
            userIdToChannelMap.remove(userId);
            channelToUserIdMap.remove(channel);
            System.out.println("Client disconnected - User ID: " + userId);
        }

        System.out.println("Channel inactive: " + channel.remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Channel senderChannel = ctx.channel();
        Content content = gson.fromJson(msg, Content.class);
        switch (content.getType()) {
            case LOGIN:
                // 登录
                Users user = gson.fromJson(gson.toJson(content.getData()), Users.class);
                Users loginuser = usersService.login(user.getId(), user.getPassword());
                //此处若loginuser为空，说明没找到该id和password对应的账号
                if(loginuser == null) {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "login failed\n")));
                    return;
                }
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.LOGIN, loginuser)));

                // 将用户 ID 和 Channel 关联起来
                userIdToChannelMap.put(user.getId(), senderChannel);
                channelToUserIdMap.put(senderChannel, user.getId());
                break;
            case REGISTER:
                Users newUser = gson.fromJson(gson.toJson(content.getData()), Users.class);
                newUser = usersService.register(newUser);
                if(newUser == null) {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "register failed\n")));
                    return;
                }
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.REGISTER, newUser)));
                break;
            case CONNECT:
                // 连接
                String tokenStr = gson.fromJson(gson.toJson(content.getData()), String.class);
                System.out.println("tokenStr: " + tokenStr);
                Long userId = usersService.getUserIdByToken(tokenStr);
                System.out.println("userId: " + userId);
                if(userId == null) {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "connect failed\n")));
                    senderChannel.close();
                    return;
                }
                userIdToChannelMap.put(userId, senderChannel);
                channelToUserIdMap.put(senderChannel, userId);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "connect success\n")));
                break;

            case MESSAGE:
                // 发送消息
                Messages message = gson.fromJson(gson.toJson(content.getData()), Messages.class);

                Long receiverId = message.getReceiverId();
                //通过channelToUserIdMap获取发送者的id
                Long senderId = channelToUserIdMap.get(senderChannel);
                // 设置会话 ID,为senderId和receiverId的组合
                message.setConversationId(senderId.toString() + "_" + receiverId.toString());
                boolean isSuccess = messagesService.addMessage(message);
                if(isSuccess) {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "send message success\n")));
                    Channel receiverChannel = userIdToChannelMap.get(receiverId);
                    if(receiverChannel != null) {
                        receiverChannel.writeAndFlush(msg);
                    }
                } else {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "send message failed\n")));
                }
                break;
            case CHATHISTORY:
                Messages messages = gson.fromJson(gson.toJson(content.getData()), Messages.class);
                Long senderId1 = messages.getSenderId();
                Long receiverId1 = messages.getReceiverId();
                String conversationId = senderId1.toString() + "_" + receiverId1.toString();
                IPage<Messages> chatHistory = messagesService.getMessagesByConversationId(1L,5L,conversationId);
                System.out.println("chatHistory: " + chatHistory);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.CHATHISTORY, chatHistory)));
            default:
                break;
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        if (cause instanceof IOException) {
            System.out.println("Client connection lost: " + channel.remoteAddress());
        } else {
            cause.printStackTrace();
        }

        // 清理资源
        Long userId = channelToUserIdMap.get(channel);
        if (userId != null) {
            userIdToChannelMap.remove(userId);
            channelToUserIdMap.remove(channel);
            System.out.println("Cleaned up resources for user: " + userId);
        }

        // 关闭连接
        if (channel.isActive()) {
            ctx.close();
        }
    }
}