package org.soso.ledger.interceptor;

import org.slf4j.MDC;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.soso.ledger.common.UserContext;
import org.soso.ledger.config.JwtUtil;
import org.soso.ledger.exception.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil; // 注入进来
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1.预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }


        // 2. 从请求头获取 Token（标准格式：Authorization: Bearer xxxxx）
        String token = request.getHeader("Authorization");
        // 3.校验token是否有效
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String redisKey = "login:token:" + token;
        String redisUserId = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisUserId == null) {
            // 如果 Redis 里找不到，直接抛出异常，拦截请求
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        // 4. 解析 JWT，拿到 userId
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = claims.get("userId", Long.class);//object->long类型
            String username= claims.get("username", String.class);
            MDC.put("userId", userId != null ? userId.toString() : "unknown");
            MDC.put("username", username != null ? username : "unknown");
            UserContext.setCurrentUserId(userId);
        } catch (Exception e) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        return true; // 放行
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        MDC.clear();
    }
}
