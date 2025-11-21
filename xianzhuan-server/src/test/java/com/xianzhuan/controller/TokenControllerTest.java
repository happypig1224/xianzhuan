package com.xianzhuan.controller;

import com.xianzhuan.result.Result;
import com.xianzhuan.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenController 单元测试")
class TokenControllerTest {

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TokenController tokenController;

    private Map<String, String> mockTokens;

    @BeforeEach
    void setUp() {
        mockTokens = new HashMap<>();
        mockTokens.put("accessToken", "testAccessToken");
        mockTokens.put("refreshToken", "testRefreshToken");
    }

    @Test
    @DisplayName("用户端刷新Token - 成功")
    void refreshUserToken_success() {
        // 1. 准备数据
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "validRefreshToken");
        when(tokenService.refreshUserToken("validRefreshToken")).thenReturn(mockTokens);

        // 2. 调用方法
        Result<Map<String, String>> result = tokenController.refreshUserToken(request);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertEquals("testAccessToken", result.getData().get("accessToken"));
        verify(tokenService).refreshUserToken("validRefreshToken");
    }

    @Test
    @DisplayName("管理端刷新Token - 成功")
    void refreshAdminToken_success() {
        // 1. 准备数据
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "adminRefreshToken");
        when(tokenService.refreshAdminToken("adminRefreshToken")).thenReturn(mockTokens);

        // 2. 调用方法
        Result<Map<String, String>> result = tokenController.refreshAdminToken(request);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(tokenService).refreshAdminToken("adminRefreshToken");
    }

    @Test
    @DisplayName("用户端登出 - 成功")
    void userLogout_success() {
        // 1. 准备数据
        String accessToken = "userAccessToken";
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "userRefreshToken");
        doNothing().when(tokenService).userLogout(anyString(), anyString());

        // 2. 调用方法
        Result<Void> result = tokenController.userLogout(accessToken, request);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(tokenService).userLogout(accessToken, "userRefreshToken");
    }

    @Test
    @DisplayName("用户端登出 - request为null")
    void userLogout_requestNull() {
        // 1. 准备数据
        String accessToken = "userAccessToken";
        doNothing().when(tokenService).userLogout(anyString(), any());

        // 2. 调用方法
        Result<Void> result = tokenController.userLogout(accessToken, null);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(tokenService).userLogout(accessToken, null);
    }

    @Test
    @DisplayName("管理端登出 - 成功")
    void adminLogout_success() {
        // 1. 准备数据
        String accessToken = "adminAccessToken";
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "adminRefreshToken");
        doNothing().when(tokenService).adminLogout(anyString(), anyString());

        // 2. 调用方法
        Result<Void> result = tokenController.adminLogout(accessToken, request);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(tokenService).adminLogout(accessToken, "adminRefreshToken");
    }

    @Test
    @DisplayName("管理端登出 - request为null")
    void adminLogout_requestNull() {
        // 1. 准备数据
        String accessToken = "adminAccessToken";
        doNothing().when(tokenService).adminLogout(anyString(), any());

        // 2. 调用方法
        Result<Void> result = tokenController.adminLogout(accessToken, null);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(tokenService).adminLogout(accessToken, null);
    }
}