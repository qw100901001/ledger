package org.soso.ledger.handler;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soso.ledger.dto.ChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /** 在线会话表：sessionId -> WebSocketSession，必须线程安全 */
    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    // ---------------- 连接建立 ----------------
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
    System.out.println("有人上线了：" + session.getId());
        String username = extractUsername(session);
        session.getAttributes().put("username", username);
        SESSIONS.put(session.getId(), session);

        log.info("[OPEN] {} 上线，当前在线 {} 人", username, SESSIONS.size());
        broadcast(ChatMessage.system(username + " 加入了聊天室", SESSIONS.size()));
        broadcastOnlineList();
    }

    // ---------------- 收到消息 ----------------
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {

        // 1. 获取发送者名字和消息内容
        String username = (String) session.getAttributes().get("username");
        String text = message.getPayload().trim();
        if (text.isEmpty()) return;

        // 2. 处理私聊指令：/to 昵称 内容
        if (text.startsWith("/to ")) {
            // 找到第二个空格的位置（"to " 占 3 个字符，所以从索引 4 开始找）
            int sp = text.indexOf(' ', 4);
            if (sp < 0) {
                send(session, ChatMessage.system("格式错误：/to 昵称 内容", SESSIONS.size()));
                return;
            }
            String target = text.substring(4, sp);     // 提取目标昵称
            String content = text.substring(sp + 1);   // 提取私聊内容
            WebSocketSession targetSession = findSessionByUsername(target);

            if (targetSession != null && targetSession.isOpen()) {
                // 发给目标用户
                send(targetSession, ChatMessage.chat(username, "[私聊] " + content, SESSIONS.size()));
                // 给自己发一份回显，让自己知道私聊发出去了
                send(session, ChatMessage.chat(username, "[私聊发给 " + target + "] " + content, SESSIONS.size()));
            } else {
                // 目标不在线，系统提示
                send(session, ChatMessage.system("用户 " + target + " 不在线", SESSIONS.size()));
            }
            return; // 私聊逻辑结束，直接返回，不走下面的群发
        }

        // 3. 处理查看在线列表指令：/online
        if ("/online".equals(text)) {
            broadcastOnlineList();
            return;
        }

        // 4. 普通群聊消息（前端最期望的聊天逻辑）
        broadcast(ChatMessage.chat(username, text, SESSIONS.size()));
    }

    // ---------------- 连接关闭 ----------------
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = (String) session.getAttributes().get("username");
//        if (SESSIONS.remove(session.getId()) != null) {
//            log.info("[CLOSE] {} 下线，当前在线 {} 人", username, SESSIONS.size());
//            broadcast(ChatMessage.system(username + " 离开了聊天室", SESSIONS.size()));
//            broadcastOnlineList();
//        }
    }
    // ---------------- 传输错误 ----------------
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("[ERROR] sessionId={}, err={}", session.getId(), exception.getMessage());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException ignored) {
        }
    }
    // ---------------- 工具方法 ----------------
    private void broadcast(ChatMessage message) {
        String json = toJson(message);
        if (json == null) return;
        TextMessage tm = new TextMessage(json);
        SESSIONS.values().forEach(s -> {
            if (s.isOpen()) {
                try {
                    // WebSocketSession.sendMessage 非线程安全
                    synchronized (s) {
                        s.sendMessage(tm);
                    }
                } catch (IOException e) {
                    log.warn("发送失败: {}", e.getMessage());
                }
            }
        });
    }

    private void broadcastOnlineList() {
        broadcast(ChatMessage.online(onlineUsers(),SESSIONS.size()));
    }
    private void send(WebSocketSession session, ChatMessage message) {
        if (session == null || !session.isOpen()) return;
        String json = toJson(message);
        if (json == null) return;
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.warn("发送失败: {}", e.getMessage());
        }
    }

    private List<String> onlineUsers() {
        return SESSIONS.values().stream()
                .map(s -> (String) s.getAttributes().get("username"))
                .sorted()
                .toList();
    }
    private WebSocketSession findSessionByUsername(String username) {
        return SESSIONS.values().stream()
                .filter(s -> username.equals(s.getAttributes().get("username")))
                .findFirst()
                .orElse(null);
    }

    private String extractUsername(WebSocketSession session) {
        String query = session.getUri() == null ? null : session.getUri().getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                if (pair.startsWith("username=")) {
                    return pair.substring("username=".length());
                }
            }
        }
        return "guest-" + session.getId().substring(0, 4);
    }
    private String toJson(ChatMessage msg) {
        try {
            return objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            log.error("序列化失败", e);
            return null;
        }
    }


}
