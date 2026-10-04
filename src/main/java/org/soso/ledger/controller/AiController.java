package org.soso.ledger.controller;

import lombok.RequiredArgsConstructor;
import org.soso.ledger.common.Result;
import org.soso.ledger.common.UserContext;
import org.soso.ledger.entity.Transaction;
import org.soso.ledger.exception.BusinessException;
import org.soso.ledger.service.GlmAiService;
import org.soso.ledger.service.LedgerService;
import org.soso.ledger.service.CategoryService;
import org.soso.ledger.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.soso.ledger.dto.CreateTransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {

    // 1. 注入 GlmAiService
    private final GlmAiService glmAiService;
    private final ObjectMapper objectMapper;
    private final LedgerService ledgerService;
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    // 加一个线程池，避免每次 new Thread
    private static final ExecutorService SSE_POOL = Executors.newCachedThreadPool();


    // 3. 核心接口
    @PostMapping("/parse-bill")
    public Result<String> parseBill(@RequestBody Map<String, String> request) {
        // 提取前端传来的文本
        String text = request.get("text");

        // 校验是否为空
        if (text == null || text.isBlank()) {
            throw new BusinessException(400, "请输入记账内容");
        }

        // 调用 AI 服务解析
        String json = glmAiService.parseBillText(text);

        // 返回成功结果
        return Result.success(json);
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody Map<String, String> req) {
        String userMessage = req.get("message");

        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("userMessage 不能为空");
        }
        // 消息历史
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(msg("user", userMessage));

        // 最多循环 5 轮，防止模型无限调工具
        int maxRounds = 5;
        JsonNode message = null;
        for (int round = 0; round < maxRounds; round++) {
            message = glmAiService.chat(messages);
            JsonNode toolCalls = message.path("tool_calls");
            if (!toolCalls.isArray() || toolCalls.isEmpty()) {
                break; // 没有工具调用，模型已经给出最终回答
            }
            // 把 assistant 这一条（含 tool_calls）原样放回历史
            messages.add(toAssistantMap(message));
            // 逐个执行工具，结果作为 role=tool 塞回历史
            for (JsonNode call : toolCalls) {
                String toolCallId = call.path("id").asText();
                String name = call.path("function").path("name").asText();
                String argsJson = call.path("function").path("arguments").asText("{}");
                String toolResult = dispatchTool(name, argsJson);
                Map<String, Object> toolMsg = new LinkedHashMap<>();
                toolMsg.put("role", "tool");
                toolMsg.put("tool_call_id", toolCallId);
                toolMsg.put("content", toolResult);
                messages.add(toolMsg);
            }
        }
        // ---------- 最终返回 ----------
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", message.path("content").asText(""));
        data.put("messages", messages);
        return data;

    }

    //工具分发
    private String dispatchTool(String name, String argumentsJson) {

        try {
            JsonNode args = objectMapper.readTree(argumentsJson);
            switch (name) {
                case "list_ledgers": {
                    // ↓↓↓ 换成你真实查库的方法 ↓↓↓
                    Long userId = UserContext.getCurrentUserId();
                    Object ledgers = ledgerService.getMyLedgers(userId);
                    return objectMapper.writeValueAsString(ledgers);
                }
                case "list_categories": {
                    Long userId = UserContext.getCurrentUserId();
                    Long ledgerId = args.path("ledgerId").asLong();
                    // ★ 替换成你的真实方法
                    Object categories = categoryService.listByLedger(ledgerId, userId);
                    return objectMapper.writeValueAsString(categories);
                }
                case "pageTransactions": {
                    Long ledgerId = args.path("ledgerId").asLong();
                    Long userId = UserContext.getCurrentUserId();
                    // ★ 替换成你的真实方法
                    Object transactions = transactionService.pageTransactions(
                            ledgerId, userId, 1, 20);
                    return objectMapper.writeValueAsString(transactions);
                }
                case "create_transaction": {
                    return handleCreateTransaction(args);
                }
                default:
                    return "{\"error\":\"unknown tool: " + name + "\"}";
            }
        } catch (Exception e) {
            // 工具执行失败也要给模型返回一个字符串，否则第二轮没法走
            try {
                Map<String, Object> err = new LinkedHashMap<>();
                err.put("error", e.getMessage());
                return objectMapper.writeValueAsString(err);
            } catch (Exception ignore) {
                return "{\"error\":\"tool execution failed\"}";
            }
        }
    }

    // ============ 辅助方法 ============
    private Map<String, Object> msg(String role, String content) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    /**
     * 把 GLM 返回的 assistant message 转成可以放回 messages 的 Map
     */
    private Map<String, Object> toAssistantMap(JsonNode message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", "assistant");
        m.put("content", message.path("content").asText(""));

        JsonNode toolCalls = message.path("tool_calls");
        if (toolCalls.isArray() && toolCalls.size() > 0) {
            List<Map<String, Object>> calls = new ArrayList<>();
            for (JsonNode call : toolCalls) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("id", call.path("id").asText());
                c.put("type", call.path("type").asText("function"));

                Map<String, Object> fn = new LinkedHashMap<>();
                fn.put("name", call.path("function").path("name").asText());
                fn.put("arguments", call.path("function").path("arguments").asText("{}"));
                c.put("function", fn);

                calls.add(c);
            }
            m.put("tool_calls", calls);
        }
        return m;
    }
    private String handleCreateTransaction(JsonNode args){
        Long ledgerId = args.path("ledgerId").asLong();
        Long categoryId = args.hasNonNull("categoryId")
                ? args.path("categoryId").asLong() : null;
        BigDecimal amount = args.path("amount").decimalValue();
        String type = args.path("type").asText("expense").toLowerCase();
        String remark = args.path("remark").asText(null);
        String dateStr = args.path("recordTime").asText(null);

        if (categoryId == null) {
            return "{\"error\":\"缺少 categoryId，请先调用 list_categories 获取该账本分类\"}";
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "{\"error\":\"amount 必须大于 0\"}";
        }
        Long userId = UserContext.getCurrentUserId();

        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setCategoryId(categoryId);
        req.setAmount(amount);
        req.setType(type);
        req.setRemark(remark);
        req.setRecordTime(parseTime(dateStr));
        Transaction tx = transactionService.createTransaction(ledgerId, userId, req);

        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("success", true);
        ok.put("transactionId", tx.getId());
        ok.put("ledgerId", ledgerId);
        ok.put("amount", amount);
        ok.put("type", type);
        return objectMapper.writeValueAsString(ok);

    }
    private LocalDateTime parseTime(String s) {
        if (s == null || s.isBlank()) {
            return LocalDateTime.now();
        }
        if (s.length() == 10) {
            return LocalDate.parse(s).atStartOfDay();
        }
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @PostMapping(value = "/ask-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askStream(@RequestBody Map<String, String> req) {

        String userMessage = req.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            throw new BusinessException(400, "请输入内容");
        }

        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L); // 5 分钟

        // 客户端断开要清理
        emitter.onCompletion(() -> { /* log */ });
        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> { /* log */ });

        SSE_POOL.submit(() -> {
            try {
                List<Map<String, Object>> messages = new ArrayList<>();
                messages.add(msg("user", userMessage));

                glmAiService.streamChat(
                        messages,
                        // 每个 chunk 推给浏览器
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .data(chunk.replace("\n", "\\n")));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::complete,
                        emitter::completeWithError
                );
            } catch (Throwable e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
