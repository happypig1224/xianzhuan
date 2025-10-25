package com.xianzhuan.service.impl;

import com.xianzhuan.properties.JwtProperties;
import com.xianzhuan.service.TokenService;
import com.xianzhuan.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {
    
    @Autowired
    private JwtProperties jwtProperties;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_TOKEN_USER_PREFIX = "token:refresh:user:";
    private static final String REFRESH_TOKEN_ADMIN_PREFIX = "token:refresh:admin:";
    private static final String USER_TYPE_USER = "user";
    private static final String USER_TYPE_ADMIN = "admin";

    @Override
    public Map<String, String> generateUserTokens(Long userId) {
        return generateTokens(
            userId,
            jwtProperties.getUserSecretKey(),
            jwtProperties.getUserTtl(),
            jwtProperties.getUserRefreshTtl(),
            USER_TYPE_USER,
            REFRESH_TOKEN_USER_PREFIX
        );
    }

    @Override
    public Map<String, String> generateAdminTokens(Long adminId) {
        return generateTokens(
            adminId,
            jwtProperties.getAdminSecretKey(),
            jwtProperties.getAdminTtl(),
            jwtProperties.getAdminRefreshTtl(),
            USER_TYPE_ADMIN,
            REFRESH_TOKEN_ADMIN_PREFIX
        );
    }
    
    private Map<String, String> generateTokens(Long userId, String secretKey, 
                                                long accessTtl, long refreshTtl, 
                                                String userType, String refreshPrefix) {
        String accessToken = JwtUtil.createAccessToken(secretKey, accessTtl, userId, userType);
        String refreshToken = JwtUtil.createRefreshToken(secretKey, refreshTtl, userId, userType);
        
        String refreshKey = refreshPrefix + userId;
        redisTemplate.opsForValue().set(refreshKey, refreshToken, refreshTtl, TimeUnit.MILLISECONDS);
        
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        
        log.info("生成双Token成功，userId:{}", userId);
        return tokens;
    }

    @Override
    public Map<String, String> refreshUserToken(String refreshToken) {
        return refreshToken(refreshToken, jwtProperties.getUserSecretKey(), 
            jwtProperties.getUserTtl(), jwtProperties.getUserRefreshTtl(),
            USER_TYPE_USER, REFRESH_TOKEN_USER_PREFIX);
    }

    @Override
    public Map<String, String> refreshAdminToken(String refreshToken) {
        return refreshToken(refreshToken, jwtProperties.getAdminSecretKey(), 
            jwtProperties.getAdminTtl(), jwtProperties.getAdminRefreshTtl(),
            USER_TYPE_ADMIN, REFRESH_TOKEN_ADMIN_PREFIX);
    }
    
    private Map<String, String> refreshToken(String refreshToken, String secretKey, 
                                              long accessTtl, long refreshTtl, 
                                              String userType, String refreshPrefix) {
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(secretKey, refreshToken);
        } catch (Exception e) {
            log.error("RefreshToken解析失败", e);
            throw new RuntimeException("RefreshToken无效或已过期");
        }
        
        String tokenType = claims.get(JwtUtil.TOKEN_TYPE_KEY, String.class);
        if (!JwtUtil.TOKEN_TYPE_REFRESH.equals(tokenType)) {
            log.error("Token类型错误，不是RefreshToken");
            throw new RuntimeException("Token类型错误");
        }
        
        Long userId = Long.valueOf(claims.get("userId").toString());
        String refreshKey = refreshPrefix + userId;
        String storedToken = (String) redisTemplate.opsForValue().get(refreshKey);
        
        if (!refreshToken.equals(storedToken)) {
            log.error("RefreshToken不匹配，userId:{}", userId);
            throw new RuntimeException("RefreshToken无效");
        }
        
        addToBlacklist(refreshToken, secretKey);
        return generateTokens(userId, secretKey, accessTtl, refreshTtl, userType, refreshPrefix);
    }

    @Override
    public void userLogout(String accessToken, String refreshToken) {
        logout(accessToken, refreshToken, jwtProperties.getUserSecretKey(), 
            jwtProperties.getUserTtl(), REFRESH_TOKEN_USER_PREFIX);
    }

    @Override
    public void adminLogout(String accessToken, String refreshToken) {
        logout(accessToken, refreshToken, jwtProperties.getAdminSecretKey(), 
            jwtProperties.getAdminTtl(), REFRESH_TOKEN_ADMIN_PREFIX);
    }
    
    private void logout(String accessToken, String refreshToken, String secretKey, 
                       long accessTtl, String refreshPrefix) {
        if (accessToken != null) {
            addToBlacklist(accessToken, secretKey);
        }
        
        if (refreshToken != null) {
            try {
                Claims claims = JwtUtil.parseJWT(secretKey, refreshToken);
                Long userId = Long.valueOf(claims.get("userId").toString());
                String refreshKey = refreshPrefix + userId;
                redisTemplate.delete(refreshKey);
                addToBlacklist(refreshToken, secretKey);
            } catch (Exception e) {
                log.warn("登出时解析RefreshToken失败", e);
            }
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
    
    private void addToBlacklist(String token, String secretKey) {
        try {
            long expireTime = JwtUtil.getExpireTime(secretKey, token);
            long now = System.currentTimeMillis();
            long ttl = expireTime - now;
            
            if (ttl > 0) {
                String blacklistKey = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(blacklistKey, "1", ttl, TimeUnit.MILLISECONDS);
                log.info("Token加入黑名单成功");
            }
        } catch (Exception e) {
            log.error("Token加入黑名单失败", e);
        }
    }
}
