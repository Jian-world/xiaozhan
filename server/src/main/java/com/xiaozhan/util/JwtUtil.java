package com.xiaozhan.util;

import cn.hutool.core.util.IdUtil;
import com.xiaozhan.config.XiaozhanProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_JTI = "jti";

    private final XiaozhanProperties properties;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put(CLAIM_USER_ID, userId);
        claims.put(CLAIM_USERNAME, username);
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_JTI, IdUtil.fastSimpleUUID());

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date(now))
                .expiration(new Date(now + properties.getJwt().getExpire()))
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token，失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("token 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 token 中取 userId
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get(CLAIM_USER_ID);
        return userId == null ? null : Long.valueOf(String.valueOf(userId));
    }

    /**
     * 从 token 中取角色
     */
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : String.valueOf(claims.get(CLAIM_ROLE));
    }

    /**
     * 剥离前缀
     */
    public String resolveToken(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        String prefix = properties.getJwt().getPrefix();
        if (prefix != null && !prefix.isBlank() && header.startsWith(prefix)) {
            return header.substring(prefix.length()).trim();
        }
        return header.trim();
    }
}
