package org.example;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.google.gson.Gson;
import org.apache.tomcat.jni.Local;
import org.example.entity.*;
import org.example.service.IConversationService;
import org.example.service.IMemberService;
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
    private IMemberService memberService;
    private IConversationService conversationService;
    private Gson gson;


    public NettyServerHandler() {
        // 通过 SpringUtils 获取 Service 实例
        this.messagesService = SpringUtils.getBean(IMessagesService.class);
        this.usersService = SpringUtils.getBean(IUsersService.class);
        this.memberService = SpringUtils.getBean(IMemberService.class);
        this.conversationService = SpringUtils.getBean(IConversationService.class);
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
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.LOGIN, null)));
                    return;
                }
                //生成token并赋值给user
                String token = java.util.UUID.randomUUID().toString();
                loginuser.setToken(token);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.LOGIN, loginuser)));

                // 将用户 ID 和 Channel 关联起来
                userIdToChannelMap.put(user.getId(), senderChannel);
                channelToUserIdMap.put(senderChannel, user.getId());
                break;
            case REGISTER:
                Users newUser = gson.fromJson(gson.toJson(content.getData()), Users.class);
                newUser = usersService.register(newUser);
                if(newUser == null) {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.REGISTER, null)));
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
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.CONNECT, null)));
                    return;
                }
                userIdToChannelMap.put(userId, senderChannel);
                channelToUserIdMap.put(senderChannel, userId);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "CONNECT success\n")));
                break;

            case MESSAGE:
                // 发送消息
                Messages message = gson.fromJson(gson.toJson(content.getData()), Messages.class);

                Long receiverId = message.getReceiverId();
                //通过channelToUserIdMap获取发送者的id
                Long senderId = channelToUserIdMap.get(senderChannel);
                //得到当前连接用户的id，判断发送人是否正确
//                if(channelToUserIdMap.get(senderChannel) != senderId) {
//                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.MESSAGE, "MESSAGE failed\n")));
//                    return;
//                }
                //此处由服务器填充id，返回给客户端
                message = messagesService.addMessage(message);
                if(message != null) {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.MESSAGE, message)));
                    Channel receiverChannel = userIdToChannelMap.get(receiverId);
                    if(receiverChannel != null) {
                        //发送给接收方
                        receiverChannel.writeAndFlush(new Content(ContentType.MESSAGE, gson.toJson(message)));
                    }
                } else {
                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.MESSAGE, "MESSAGE failed\n")));
                }
                break;
            case OLDCHATRECORD:
                // 获取旧聊天记录
                Messages message1 = gson.fromJson(gson.toJson(content.getData()), Messages.class);
                String conversationId = message1.getConversationId();
                Long messageId = message1.getId();
                IPage<Messages> chatBefore = messagesService.getOldMessagesByConversationId(conversationId, messageId);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.OLDCHATRECORD, chatBefore)));
                break;
            case NEWCHATRECORD:
                // 获取新聊天记录
               Messages message2 = gson.fromJson(gson.toJson(content.getData()), Messages.class);
                String conversationId1 = message2.getConversationId();
                Long messageId1 = message2.getId();
                List<Messages> newChatRecord = messagesService.getNewMessagesByConversationId(conversationId1, messageId1);
                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.NEWCHATRECORD, newChatRecord)));
                    break;
            case CONVERSATIONINFO:
                // 查询会话信息
                Member member = gson.fromJson(gson.toJson(content.getData()), Member.class);
                Long userId1 = member.getUserId();

                //与请求方id有关的所有Member
                List<Member> members = memberService.getMemberByUserId(userId1);
                List<ConversationInfo> conversationInfos = new ArrayList<>();

                //收集会话信息，构造ConversationInfo
                for(Member m : members) {
                    //获得有关的会话id
                    String conversationId3 = m.getConversationId();
                    Conversation conversation3 = conversationService.getById(conversationId3);
                    LocalDateTime lastMessageTime = conversation3.getLastMessageTime();
                    String lastmessage = conversation3.getLastMessage();
                    Byte status = conversation3.getStatus();

                    //如果是私聊
                    if(conversation3.getConversationType() == 0) {
                        //得到对方id
                        Long friendId;
                        List<Member> members1 = memberService.getMemberByConversationId(conversationId3);
                        //最后阅读该会话的消息id
                        Long lastReadId;
                        //member1是一个对话的双方,即两条member记录
                        if(members1.get(0).getUserId() == userId1) {
                            lastReadId = members1.get(0).getLastReadId();
                            friendId = members1.get(1).getUserId();
                        } else {
                            lastReadId = members1.get(1).getLastReadId();
                            friendId = members1.get(0).getUserId();
                        }
                        //朋友的实体类
                        Users user1 = usersService.getById(friendId);
                        int unreadCount = messagesService.getNewMessagesByConversationId(conversationId3, lastReadId).size();
                        String username1 = user1.getUsername();
                        String userAvatar1 = user1.getAvatarUrl();
                        ConversationInfo conversationInfo = new ConversationInfo(conversationId3, username1, userAvatar1, lastmessage, lastMessageTime, unreadCount,friendId, status);
                        conversationInfos.add(conversationInfo);
                        System.out.println("conversationInfo: " + conversationInfo.toString());
                    }

                    //如果是群聊
                    else if(conversation3.getConversationType() == 1) {
                        String name = conversation3.getTitle();
                        String avatar = conversation3.getAvatarUrl();
                        Member member1 = memberService.getMember(conversationId3, userId1);

                        int unreadcount = messagesService.getNewMessagesByConversationId(conversationId3, member1.getLastReadId()).size();
                        ConversationInfo conversationInfo = new ConversationInfo(conversationId3, name, avatar, lastmessage, lastMessageTime, unreadcount,null, status);
                        conversationInfos.add(conversationInfo);
                        System.out.println("conversationInfo: " + conversationInfo.toString());
                    }
                }

                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.CONVERSATIONINFO, conversationInfos)));
                break;
            case CREATEPRIVATECONVERSATION:
                String receiverIdStr = gson.fromJson(gson.toJson(content.getData()), String.class);
                Long receiverId1 = Long.parseLong(receiverIdStr);
                System.out.println(receiverId1);
                //创建私聊会话
                Conversation conversation2 = new Conversation();
                conversation2.setConversationId(java.util.UUID.randomUUID().toString());
                conversation2.setConversationType((byte)0);
                conversation2.setStatus((byte)0);
                conversationService.save(conversation2);

                //创建两条member记录
                Member member1 = new Member();
                member1.setConversationId(conversation2.getConversationId());
                member1.setUserId(channelToUserIdMap.get(senderChannel));

                Member member2 = new Member();
                member2.setConversationId(conversation2.getConversationId());
                member2.setUserId(receiverId1);

                memberService.save(member1);
                memberService.save(member2);

                break;
            case PRIVATECONVERSATIONRESPONSE:
                Conversation conversation4 = gson.fromJson(gson.toJson(content.getData()), Conversation.class);
                Byte status = conversation4.getStatus();
                //如果拒绝
                if(status == 0) {
                    //删除之前相关的member和conversation
                    //因为设置了级联删除，所以只需要删除conversation即可
                    conversationService.removeById(conversation4.getConversationId());
                }
                else if(status == 1) {
                    //接受
                    conversation4.setStatus((byte)1);
                    conversationService.updateById(conversation4);
                }
                break;
            case INITCONVERSATION:
                Conversation conversation = gson.fromJson(gson.toJson(content.getData()), Conversation.class);
                String conversationId2 = conversation.getConversationId();
                LambdaQueryWrapper<Messages> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Messages::getConversationId, conversationId2)
                            .lt(Messages::getCreatedAt, LocalDateTime.now())
                            .orderByDesc(Messages::getCreatedAt);
                IPage<Messages> messages = messagesService.page(new Page<>(1, 5), queryWrapper);
                System.out.println("messages: " + messages.getRecords());

                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.INITCONVERSATION, messages)));
                break;


//            case FRIENDREQUEST:
//                Friends friend = gson.fromJson(gson.toJson(content.getData()), Friends.class);
//                //判断发送者是否为当前连接用户
////                if(channelToUserIdMap.get(senderChannel) != friend.getUserId()) {
////                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "send friend request failed\n")));
////                    return;
////                }
//                friend.setStatus((byte)0);
//                boolean isFriend = friendsService.addFriendRecord(friend);
//                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, isFriend?"FRIENDREQUEST success\n":"FRIENDREQUEST failed\n")));
//                break;
//            case FRIENDRESPONSE:
//                Friends responseFriend = gson.fromJson(gson.toJson(content.getData()), Friends.class);
//                //判断发送者是否为当前连接用户
////                if(channelToUserIdMap.get(senderChannel) != responseFriend.getUserId()) {
////                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "response friend request failed\n")));
////                    return;
////                }
//                boolean isResponse = friendsService.updateFriendRecord(responseFriend);
//                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, isResponse?"FRIENDREQUEST success\n":"FRIENDREQUEST failed\n")));
//               break;
//            case FRIENDQUERY:
//                Friends queryFriend = gson.fromJson(gson.toJson(content.getData()), Friends.class);
//                //判断发送者是否为当前连接用户
////                if(channelToUserIdMap.get(senderChannel) != queryFriend.getUserId()) {
////                    senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.SERVERRESPONSE, "FRIENDQUERY failed\n")));
////                    return;
////                }
//                List<Friends> resultFriend = friendsService.getFriendsByUserId(queryFriend.getUserId());
//                for(Friends f : resultFriend) {
//                    System.out.println(f);
//                }
//                //根据friend的id返回信息,遍历
//                List<ConversationInfo> friendsInfo = new ArrayList<>();
//                for(Friends f : resultFriend) {
//                    //得到朋友的id
//                    Long friendId;
//                    if(f.getFriendId() == queryFriend.getUserId()) {
//                        friendId = f.getUserId();
//                    } else {
//                        friendId = f.getFriendId();
//                    }
//
//                    Users user1 = usersService.getById(friendId);
//                    System.out.println("user1: " + user1);
//                    ConversationInfo friendInfo = new ConversationInfo();
//                    friendInfo.setUserId(user1.getId());
//                    friendInfo.setUserName(user1.getUsername());
//                    friendInfo.setUserAvatar(user1.getAvatarUrl());
//                    List<Messages> messages1= messagesService.getMessagesByConversationId(100L,1L,5L,user1.getId().toString()+"_"+queryFriend.getUserId().toString()).getRecords();
//                    if(messages1.size() > 0) {
//                        friendInfo.setLastMessage(messages1.get(0).getMessageContent());
//                        friendInfo.setLastMessageTime(messages1.get(0).getCreatedAt());
//                    }
//                    System.out.println("friendInfo: " + friendInfo);
//                    friendsInfo.add(friendInfo);
//                }
//
//                senderChannel.writeAndFlush(gson.toJson(new Content(ContentType.FRIENDQUERY, friendsInfo)));
//                break;
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