package org.soso.ledger.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.soso.ledger.config.JwtUtil;
import org.soso.ledger.dto.LoginRequest;
import org.soso.ledger.dto.RegisterRequest;
import org.soso.ledger.dto.UserDTO;
import org.soso.ledger.entity.User;
import org.soso.ledger.exception.BusinessException;
import org.soso.ledger.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void register(RegisterRequest req) {
        // MyBatis-Plus 写法：链式查询
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        );

        if (existingUser != null) {
            log.warn("注册失败，用户名已存在：{}", req.getUsername());
            throw new BusinessException(40002, "用户名已存在");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        if (req.getNickname() == null || req.getNickname().isBlank()) {
            user.setNickname(req.getUsername());
        } else {
            user.setNickname(req.getNickname());
        }
        userMapper.insert(user);
        log.info("用户注册成功：userId={}, username={}", user.getId(), user.getUsername());  // ★ 加这
    }

    public String login(LoginRequest req,String ip) {

        // 1. 【高并发优化】Redis 限流：同一 IP 一分钟最多 5 次登录请求
        String limitKey = "limit:login:ip:" + ip;
        Long count = stringRedisTemplate.opsForValue().increment(limitKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(limitKey, 1, TimeUnit.MINUTES);
        }
        if (count != null && count > 5) {
            throw new BusinessException(429, "登录请求过于频繁，请一分钟后再试");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        );

        if (user == null) {
            throw new BusinessException(40003, "用户名或密码错误");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(40003, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        stringRedisTemplate.opsForValue().set("login:token:" + token, user.getId().toString(), 30, TimeUnit.HOURS);
        return token;
    }

    /**
     * 签发 AI 专用长效 token：JWT 与 Redis 会话均 30 天。
     * 复用 login:token: 键位，AuthInterceptor 零改动即可校验。
     */
    public Map<String, Object> issueAiToken(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(40003, "用户不存在");
        }
        String token = jwtUtil.generateAiToken(user.getId(), user.getUsername());
        stringRedisTemplate.opsForValue().set("login:token:" + token, user.getId().toString(), 30, TimeUnit.DAYS);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("expiresInDays", 30);
        return data;
    }

    public void logout(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        stringRedisTemplate.delete("login:token:" + token);
    }

    public UserDTO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        return dto;
    }
}
