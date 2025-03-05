package org.example;

import org.example.entity.Content;
import org.example.entity.ContentType;
import org.example.entity.Messages;

public class Client {
    public static void main(String[] args) throws Exception {
        NettyClient client = new NettyClient("localhost", 8888);
        client.start();
//        Content content = new Content(ContentType.LOGIN, new Users(1L, "123456"));
        Content content = new Content(ContentType.CONNECT,"ac");
        client.sendMessage(content);

        String messagecontent ;
        while(true){
           //询问用户输入
            System.out.println("请输入要发送的消息：");
            messagecontent = new java.util.Scanner(System.in).nextLine();
            if(messagecontent.equals("exit")){
                break;
            }
            content = new Content(ContentType.MESSAGE, new Messages(2L, 1L, messagecontent));
            client.sendMessage(content);
        }
    }
}
