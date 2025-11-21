package com.xianzhuan.controller.user;

import com.xianzhuan.dto.UserLoginDTO;
import com.xianzhuan.entity.User;
import com.xianzhuan.properties.JwtProperties;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.TokenService;
import com.xianzhuan.service.UserService;
import com.xianzhuan.vo.UserLoginVO;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController 单元测试")
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserController userController;

    private User mockUser;
    private Map<String, String> mockTokens;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setOpenid("testOpenid");

        mockTokens = new HashMap<>();
        mockTokens.put("accessToken", "testAccessToken");
        mockTokens.put("refreshToken", "testRefreshToken");
    }

    @Test
    @DisplayName("微信登录 - 成功")
    void wxLogin_success() {
        // 1. 准备数据
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setCode("testCode");
        when(userService.wxLogin(any(UserLoginDTO.class))).thenReturn(mockUser);
        when(tokenService.generateUserTokens(1L)).thenReturn(mockTokens);

        // 2. 调用方法
        Result<UserLoginVO> result = userController.wxLogin(loginDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
        assertEquals("testOpenid", result.getData().getOpenid());
        assertEquals("testAccessToken", result.getData().getToken());
        assertEquals("testRefreshToken", result.getData().getRefreshToken());
        verify(userService).wxLogin(any(UserLoginDTO.class));
        verify(tokenService).generateUserTokens(1L);
    }
}