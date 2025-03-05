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
    CHATHISTORY,  //用于查询和返回聊天记录
}