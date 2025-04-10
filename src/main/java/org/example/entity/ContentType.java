package org.example.entity;

//消息类型
public enum ContentType {
    REGISTER,  //注册
    LOGIN,  //登录
    LOGOUT,  //登出
    MESSAGE,  //发送消息
    CONNECT,  //已登录用户连接服务器，此处通过验证{token]来判断用户是否已登录
    SYSTEM,
    SERVERRESPONSE,  //简单的服务器响应消息，比如"Login success"
    INITCONVERSATION,  //初始化某个会话的聊天，主要防止客户端不知道最新的聊天记录id
    NEWCHATRECORD,  //某个会话的全部新聊天记录，用messages表的id查询
    OLDCHATRECORD,  //某个会话的全部旧聊天记录
    CONVERSATIONINFO,  //查询会话信息
    CREATEPRIVATECONVERSATION,  //创建和邀请进入私聊会话
    PRIVATECONVERSATIONRESPONSE,  //用户对私聊的响应
    CREATEGROUPCONVERSATION,  //创建群聊
    INVITEGROUPCONVERSATION,  //邀请进入群聊
    GROUPCONVERSATIONRESPONSE,  //用户对群聊邀请的响应
}