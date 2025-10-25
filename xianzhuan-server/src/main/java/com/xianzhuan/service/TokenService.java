package com.xianzhuan.service;

import java.util.Map;

public interface TokenService {
    
    /**
     * 生成双Token
     */
    Map<String, String> generateUserTokens(Long userId);
    
    /**
     * 生成管理员双Token
     */
    Map<String, String> generateAdminTokens(Long adminId);
    
    /**
     * 刷新Token
     */
    Map<String, String> refreshUserToken(String refreshToken);
    
    /**
     * 刷新管理员Token
     */
    Map<String, String> refreshAdminToken(String refreshToken);
    
    /**
     * 用户登出
     */
    void userLogout(String accessToken, String refreshToken);
    
    /**
     * 管理员登出
     */
    void adminLogout(String accessToken, String refreshToken);
    
    /**
     * 检查AccessToken是否在黑名单
     */
    boolean isTokenBlacklisted(String token);
    
}
