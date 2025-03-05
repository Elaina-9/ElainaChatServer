package org.example;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.entity.Content;
import org.example.entity.ContentType;
import org.example.entity.Messages;
import org.example.entity.Users;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class NettyClientHandler extends SimpleChannelInboundHandler<String> {
    private Channel channel;
    private Gson gson = CustomGson.getCustomGson();
    private NettyClient client;

    public NettyClientHandler(NettyClient client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Content content = gson.fromJson(msg, Content.class);
        ContentType type = content.getType();
        switch(type) {
            case REGISTER:
                Users newUser = gson.fromJson(gson.toJson(content.getData()), Users.class);
                System.out.println("Registered user: " + newUser.toString());
                break;
            case LOGIN:
                Users user = gson.fromJson(gson.toJson(content.getData()), Users.class);
                System.out.println(user.toString());
                break;
            case MESSAGE:
                Messages message = gson.fromJson(gson.toJson(content.getData()), Messages.class);
                System.out.println("Received message: " + message.getMessageContent());
                break;
            case SERVERRESPONSE:
                System.out.println("Server response: " + content.getData().toString());
                break;
            case CHATHISTORY:
                Type pageType = new TypeToken<IPage<Messages>>(){}.getType();
                IPage<Messages> messages = gson.fromJson(gson.toJson(content.getData()), pageType);
                for(Messages m : messages.getRecords()) {
                    System.out.println(m.getSenderId() + "  send to " + m.getReceiverId() + " : " + m.getMessageContent());
                }
                break;
        }
        //System.out.println("Server response: " + message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connected to server");
        this.channel = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Disconnected from server");
        if(this.channel != null && this.channel.isActive()) {
            this.channel.close();
        }
        //关闭EventLoopGroup
        ctx.channel().eventLoop().shutdownGracefully();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}