package com.example.chatsystem; // 修改包名

public class ChatMessage {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;

    private String sender;
    private String content;
    private String time;
    private int type; // 消息类型：发送还是接收

    public ChatMessage(String sender, String content, String time, int type) {
        this.sender = sender;
        this.content = content;
        this.time = time;
        this.type = type;
    }

    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public int getType() { return type; }
}