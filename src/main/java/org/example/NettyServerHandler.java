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
import org.example.entity.*;
import org.example.service.IFriendsService;
import org.example.service.IMessagesService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.example.entity.Content;
import org.example.service.IUsersService;

public class NettyServerHandler extends SimpleChannelInboundHandler<String> {
    // 用户id和Channel的映射关系
    private static final Map<Long, Channel> userIdToChannelMap = new ConcurrentHashMap<>();
    private static final Map<Channel, Long> channelToUserIdMap = new ConcurrentHashMap<>();

    private IMessagesService messagesService;
    private IUsersService usersService;
    private IFriendsService friendsService;
    private Gson gson;


    public NettyServerHandler() {
        // 通过 SpringUtils 获取 Service 实例
        this.messagesService = SpringUtils.getBean(IMessagesService.class);
        this.usersService = SpringUtils.getBean(IUsersService.class);
        this.friendsService = SpringUtils.getBean(IFriendsService.class);
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
                //得到当前连接用户的id，判断发送人是否正确
                if(channelToUserIdMap.get(senderChannel) != senderId) {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "send message failed\n")));
                    return;
                }
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
                //此处Message内容为lastMessageId_currentPage
                String[] messageContent = messages.getMessageContent().split("_");
                Long lastMessageId = Long.parseLong(messageContent[0]);
                Long currentPage = Long.parseLong(messageContent[1]);

                IPage<Messages> chatHistory = messagesService.getMessagesByConversationId(lastMessageId,currentPage,5L,conversationId);
                System.out.println("chatHistory: " + chatHistory);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.CHATHISTORY, chatHistory)));
                break;
            case FRIENDREQUEST:
                Friends friend = gson.fromJson(gson.toJson(content.getData()), Friends.class);
                //判断发送者是否为当前连接用户
//                if(channelToUserIdMap.get(senderChannel) != friend.getUserId()) {
//                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "send friend request failed\n")));
//                    return;
//                }
                friend.setStatus((byte)0);
                boolean isFriend = friendsService.addFriendRecord(friend);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, isFriend?"send friend request success\n":"send friend request failed\n")));
                break;
            case FRIENDRESPONSE:
                Friends responseFriend = gson.fromJson(gson.toJson(content.getData()), Friends.class);
                //判断发送者是否为当前连接用户
//                if(channelToUserIdMap.get(senderChannel) != responseFriend.getUserId()) {
//                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "response friend request failed\n")));
//                    return;
//                }
                boolean isResponse = friendsService.updateFriendRecord(responseFriend);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, isResponse?"response friend request success\n":"response friend request failed\n")));
               break;
            case FRIENDQUERY:
                Friends queryFriend = gson.fromJson(gson.toJson(content.getData()), Friends.class);
                //判断发送者是否为当前连接用户
//                if(channelToUserIdMap.get(senderChannel) != queryFriend.getUserId()) {
//                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "query friend failed\n")));
//                    return;
//                }
                List<Friends> resultFriend = friendsService.getFriendsByUserId(queryFriend.getUserId());
                for(Friends f : resultFriend) {
                    System.out.println(f);
                }
                //根据friend的id返回信息,遍历
                List<FriendsInfo> friendsInfo = new ArrayList<>();
                for(Friends f : resultFriend) {
                    //得到朋友的id
                    Long friendId;
                    if(f.getFriendId() == queryFriend.getUserId()) {
                        friendId = f.getUserId();
                    } else {
                        friendId = f.getFriendId();
                    }

                    Users user1 = usersService.getById(friendId);
                    System.out.println("user1: " + user1);
                    FriendsInfo friendInfo = new FriendsInfo();
                    friendInfo.setUserId(user1.getId());
                    friendInfo.setUserName(user1.getUsername());
                    friendInfo.setUserAvatar(user1.getAvatarUrl());
                    List<Messages> messages1= messagesService.getMessagesByConversationId(100L,1L,5L,user1.getId().toString()+"_"+queryFriend.getUserId().toString()).getRecords();
                    if(messages1.size() > 0) {
                        friendInfo.setLastMessage(messages1.get(0).getMessageContent());
                        friendInfo.setLastMessageTime(messages1.get(0).getCreatedAt());
                    }
                    System.out.println("friendInfo: " + friendInfo);
                    friendsInfo.add(friendInfo);
                }

                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.FRIENDQUERY, friendsInfo)));
                break;
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