package org.soso.ledger.service;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import jakarta.validation.Valid;
import tools.jackson.databind.JsonNode;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import java.util.*;

@Service
public class GlmAiService {
    // 1. 从配置文件里读取 GLM 的 API Key 和 URL
    @Value("${glm.api-key}")
    private String apiKey;

    @Value("${glm.api-url}")
    private String apiUrl;
    @Value("${glm.api-model}")
    private String model;

    // 2. 初始化 HTTP 客户端和 JSON 解析器
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GlmAiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String parseBillText(String userInput) {
        System.out.println("读取到的 GLM Key 长度: " + (apiKey != null ? apiKey.length() : "null"));
        // 3. 构造请求头（包含认证的 API Key）
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // 自动加上 Bearer 前缀
        // 4. 核心 Prompt 设计（非常关键，要求 AI 返回严格的 JSON）
        String prompt = "你是一个记账助手。请从用户的语句中提取记账信息，并严格以JSON格式返回，不要包含任何Markdown代码块标记或其他文字。\n" +
                "需要提取的字段：\n" +
                "- amount (金额，数字类型)\n" +
                "- type (类型，只能是 'expense' 支出 或 'income' 收入)\n" +
                "- category (分类，如：餐饮、交通、购物等)\n" +
                "- date (日期，格式 YYYY-MM-DD，如果用户没写，返回今天)\n" +
                "- remark (备注，简短的描述)\n\n" +
                "用户语句：" + userInput;

        // 5. 构造 GLM 兼容 OpenAI 格式的请求体
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model); // 使用极速版，便宜且快，适合提取任务
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.1); // 极低温度，防止 AI 胡说八道，保证输出稳定

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());

            // 提取 AI 返回的内容
            String jsonResult = root.path("choices").get(0).path("message").path("content").asText();

            // 7. 容错处理：防止 AI 偶尔不听话加了 ```json ``` 包裹
            jsonResult = jsonResult.replace("```json", "").replace("```", "").trim();

            return jsonResult;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI 解析失败: " + e.getMessage(), e);
        }
    }

    public JsonNode askAi(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("userMessage 不能为空");
        }
        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        return chat(Collections.singletonList(userMsg));
    }

    /**
     * 新方法：接收完整 messages 列表（第一轮、第二轮都走这里）
     */
    public JsonNode chat(List<Map<String, Object>> messages) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("tools", buildTools());
        body.put("tool_choice", "auto");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(apiUrl, entity, String.class);
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message");
            }
            throw new IllegalStateException("GLM 响应缺少 choices: " + response);
        } catch (Exception e) {
            throw new IllegalStateException("解析 GLM 响应失败: " + response, e);
        }
    }

    /**
     * 拼 tools 数组
     */
    private Map<String, Object> buildTool(String name, String description, Map<String, Object> properties, List<String> required) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type", "object");
        params.put("properties", properties != null ? properties : new LinkedHashMap<String, Object>());
        params.put("required", required != null ? required : new ArrayList<String>());

        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", params);

        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", "function");
        tool.put("function", function);

        return tool;
    }

    private List<Map<String, Object>> buildTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        // ===== 1. list_ledgers：查账本（无参数）=====
        tools.add(buildTool(
                "list_ledgers",
                "查询当前登录用户的所有记账账本，返回账本 id 和名称的列表。" +
                        "当用户询问自己有哪些账本，或回答需要用到账本 id 时调用此工具。",
                null, null
        ));
        //{type:function,function:()=>{name:111,descrition:"3434",}}

        // 通用属性：ledgerId
        Map<String, Object> ledgerIdProp = new LinkedHashMap<>();
        ledgerIdProp.put("type", "integer");
        ledgerIdProp.put("description", "账本 ID。可通过 list_ledgers 工具获取。");

        // ===== 2. list_categories：查某账本下的分类 =====
        Map<String, Object> catProps = new LinkedHashMap<>();
        catProps.put("ledgerId", ledgerIdProp);
        tools.add(buildTool(
                "list_categories",
                "【分类工具】查询指定账本下的所有分类（也叫种类、类别、账目类型），返回分类 id 和名称列表\n" +
                        "注意：本工具查询的是'分类'，不是账本本身，不要和 list_ledgers 混淆。" +
                        "当用户问某个账本有哪些分类、种类、类别时调用。" +
                        "不知道 ledgerId 时，先调用 list_ledgers 获取。",
                catProps,
                List.of("ledgerId")
        ));
        // 通用属性：categoryId / 日期
        Map<String, Object> categoryIdProp = new LinkedHashMap<>();
        categoryIdProp.put("type", "integer");
        categoryIdProp.put("description", "分类 ID。可选。");

        Map<String, Object> startDateProp = new LinkedHashMap<>();
        startDateProp.put("type", "string");
        startDateProp.put("description", "开始日期，格式 yyyy-MM-dd。可选。");

        Map<String, Object> endDateProp = new LinkedHashMap<>();
        endDateProp.put("type", "string");
        endDateProp.put("description", "结束日期，格式 yyyy-MM-dd。可选。");

        // ===== 3. pagetransactions：查流水（可选筛选）=====
        Map<String, Object> txQueryProps = new LinkedHashMap<>();
        txQueryProps.put("ledgerId", ledgerIdProp);
        txQueryProps.put("categoryId", categoryIdProp);
        txQueryProps.put("startDate", startDateProp);
        txQueryProps.put("endDate", endDateProp);

        tools.add(buildTool(
                "pageTransactions",
                "查询指定账本下的账单流水，可以按分类、日期范围筛选。" +
                        "当用户问'某账本的花销'、'最近支出'、'上个月的账单'时调用。" +
                        "如果不知道 ledgerId，先调用 list_ledgers 获取。",
                txQueryProps,
                List.of("ledgerId")
        ));

        // 通用属性：金额 / 类型 / 备注 / 日期
        Map<String, Object> amountProp = new LinkedHashMap<>();
        amountProp.put("type", "number");
        amountProp.put("description", "金额，正数。比如 23.5。");

        Map<String, Object> typeProp = new LinkedHashMap<>();
        typeProp.put("type", "string");
        typeProp.put("description", "类型：expense 表示支出，income 表示收入。");
        typeProp.put("enum", List.of("expense", "income"));

        Map<String, Object> remarkProp = new LinkedHashMap<>();
        remarkProp.put("type", "string");
        remarkProp.put("description", "备注或摘要，比如'午饭'。可选。");

        Map<String, Object> txDateProp = new LinkedHashMap<>();
        txDateProp.put("type", "string");
        txDateProp.put("description", "交易日期，格式 yyyy-MM-dd。可选，默认今天。");

        // ===== 4. create_transaction：记账（写操作）=====
        Map<String, Object> createTxProps = new LinkedHashMap<>();
        createTxProps.put("ledgerId", ledgerIdProp);
        createTxProps.put("amount", amountProp);
        createTxProps.put("type", typeProp);
        createTxProps.put("categoryId", categoryIdProp);
        createTxProps.put("remark", remarkProp);
        createTxProps.put("recordTime", txDateProp);

        tools.add(buildTool(
                "create_transaction",
                "记录一笔新的账单流水（写操作）。" +
                        "当用户明确表示要记一笔账时调用，比如'昨天午饭 23 块'、'今天打车花了 50'。" +
                        "如果不知道 ledgerId，先调用 list_ledgers 获取；" +
                        "如果用户没指明账本，不要自己猜，先问用户。",
                createTxProps,
                List.of("ledgerId", "amount", "type")
        ));

        return tools;
    }

    /**
     * 流式对话。
     * 暂不带 tools，先把 SSE 链路跑通。
     */
    public void streamChat(List<Map<String, Object>> messages,
                           Consumer<String> onChunk,
                           Runnable onDone,
                           Consumer<Throwable> onError) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", true);   // ★ 关键
        try {
            restTemplate.execute(
                    apiUrl,
                    HttpMethod.POST,
                    req -> {
                        req.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        req.getHeaders().setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
                        req.getHeaders().setBearerAuth(apiKey);
                        objectMapper.writeValue(req.getBody(), body);
                    },
                    resp -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(resp.getBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.isBlank()) continue;
                                if (!line.startsWith("data:")) continue;

                                String data = line.substring(5).trim();
                                if ("[DONE]".equals(data)) break;

                                JsonNode node = objectMapper.readTree(data);
                                String delta = node.path("choices")
                                        .path(0)
                                        .path("delta")
                                        .path("content")
                                        .asText("");
                                if (!delta.isEmpty()) {
                                    onChunk.accept(delta);
                                }
                            }
                        }
                        return null;
                    }
            );
            onDone.run();
        } catch (Throwable e) {
            onError.accept(e);
        }

    }
}
