package org.soso.ledger.common;

import org.soso.ledger.exception.BusinessException;

public class UserContext {
    // 使用 ThreadLocal 保存当前请求的用户ID（模拟）
    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Long getCurrentUserId() {
        // 真实场景应从 JWT Token 或 SecurityContext 中解析
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            throw new BusinessException(401,"未登录");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}