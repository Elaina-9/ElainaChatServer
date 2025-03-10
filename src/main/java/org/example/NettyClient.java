package org.example;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.example.entity.Content;
import org.example.entity.ContentType;
import org.example.entity.Messages;
import org.example.entity.Users;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NettyClient {
    private final String host;
    private final int port;
    private Channel channel;
    private final EventLoopGroup group;
    private Gson gson ;
    //用户信息
    private Users user;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.group = new NioEventLoopGroup();
        //适配LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .serializeNulls()
                .create();
    }

    public void start() throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new NettyClientHandler(NettyClient.this));
                        }
                    });

            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    public void sendMessage(Content content) {
        if (channel == null || !channel.isActive()) {
            System.out.println("Client not connected");
            return;
        }
        String jsoncontent = gson.toJson(content) + "\n";
        channel.writeAndFlush(jsoncontent);
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        group.shutdownGracefully();
    }
}

class Main {
    public static void main(String[] args) throws Exception {
        NettyClient client = new NettyClient("localhost", 8888);
        client.start();

//        Content content = new Content(ContentType.REGISTER, new Users("Ge", "123456"));
//        client.sendMessage(content);

//        Content content = new Content(ContentType.LOGIN, new Users(1L, "123456"));
//        client.sendMessage(content);

//        Content content = new Content(ContentType.CONNECT,"ac");
//        client.sendMessage(content);

        String messagecontent = "100" + "_" + "2";
        Content content = new Content(ContentType.CHATHISTORY,new Messages(1L,2L,messagecontent));
        client.sendMessage(content);

//        while(true){
//            //询问用户输入
//            System.out.println("请输入要发送的消息：");
//            String messagecontent = new java.util.Scanner(System.in).nextLine();
//            if(messagecontent.equals("exit")){
//                break;
//            }
//            content = new Content(ContentType.MESSAGE, new Messages(1L, 2L, messagecontent));
//            client.sendMessage(content);
//        }
    }
};
