package org.soso.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String type;        // system / chat / online
    private String from;        // 发送者（仅聊天消息需要）
    private String text;        // 消息内容（聊天或系统提示）
    private List<String> users; // 在线用户列表（仅在线列表消息需要）
    private int online;         // 当前在线人数

    // 工厂方法：生成系统消息
    public static ChatMessage system(String text, int online) {
        return new ChatMessage("system", "系统", text, null, online);
    }

    // 工厂方法：生成普通聊天消息
    public static ChatMessage chat(String from, String text, int online) {
        return new ChatMessage("chat", from, text, null, online);
    }

    // 工厂方法：生成在线列表消息
    public static ChatMessage online(List<String> users, int online) {
        return new ChatMessage("online", null, null, users, online);
    }
}
