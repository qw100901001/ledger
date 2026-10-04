package org.soso.ledger.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.ai-expiration:2592000000}") // AI 专用长效（毫秒），默认 30 天，可在配置覆盖
    private long aiExpiration;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret 不能为空");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // 坑：HS256 要求至少 256 bit = 32 字节。短了启动直接炸。
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret 长度不足：HS256 至少需要 32 字节，当前 " + keyBytes.length + " 字节。"
            );
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // 生成 Token
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // 生成 AI 专用长效 Token（供 ledger_skill / ledger-agent 等程序化调用，scope=ai）
    public String generateAiToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("scope", "ai");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + aiExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // 解析 Token
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}