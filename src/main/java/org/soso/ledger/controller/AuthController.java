package org.soso.ledger.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.soso.ledger.common.Result;
import org.soso.ledger.common.UserContext;
import org.soso.ledger.dto.LoginRequest;
import org.soso.ledger.dto.RegisterRequest;
import org.soso.ledger.dto.UserDTO;
import org.soso.ledger.entity.User;
import org.soso.ledger.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return Result.success(null);
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest req,HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String token = authService.login(req,ip);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // 从请求头拿 Token
        String token = request.getHeader("Authorization");
        authService.logout(token);
        return Result.success("退出成功");
    }

    /**
     * POST /auth/ai-token 签发 30 天 AI 专用 token（需先用普通 token 登录）
     */
    @PostMapping("/ai-token")
    public Result<Map<String, Object>> issueAiToken() {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(authService.issueAiToken(userId));
    }

    @GetMapping("/me")
    public Result<UserDTO> me() {
        Long userId = UserContext.getCurrentUserId();
        UserDTO user = authService.getCurrentUser(userId);
        return Result.success(user);

    }
}