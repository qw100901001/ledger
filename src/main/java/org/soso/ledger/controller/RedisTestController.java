package org.soso.ledger.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
@RestController
@RequestMapping("/")

public class RedisTestController {
    // 这里注入的是 Spring 官方包里的 StringRedisTemplate
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/redis")
    public String testRedis() {
        // 1. 存数据 (Key, Value)
        stringRedisTemplate.opsForValue().set("test:name", "我的记账本");

        // 2. 取数据
        String value = stringRedisTemplate.opsForValue().get("test:name");

        // 3. 设置过期时间（重要！比如5分钟过期）
        stringRedisTemplate.expire("test:name", 5, TimeUnit.MINUTES);

        return "从Redis拿到的值：" + value;
    }
}
