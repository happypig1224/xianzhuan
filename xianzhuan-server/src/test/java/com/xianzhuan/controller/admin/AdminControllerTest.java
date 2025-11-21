package com.xianzhuan.controller.admin;

import com.xianzhuan.dto.AdminDTO;
import com.xianzhuan.dto.AdminLoginDTO;
import com.xianzhuan.dto.AdminPageQueryDTO;
import com.xianzhuan.entity.Admin;
import com.xianzhuan.properties.JwtProperties;
import com.xianzhuan.result.PageResult;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.AdminService;
import com.xianzhuan.service.TokenService;
import com.xianzhuan.vo.AdminLoginVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController 单元测试")
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AdminController adminController;

    @Test
    @DisplayName("管理员登录 - 成功")
    void login_success() {
        // 1. 准备数据
        AdminLoginDTO loginDTO = new AdminLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setName("管理员");
        when(adminService.login(any(AdminLoginDTO.class))).thenReturn(admin);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", "adminAccessToken");
        tokens.put("refreshToken", "adminRefreshToken");
        when(tokenService.generateAdminTokens(1L)).thenReturn(tokens);

        // 2. 调用方法
        Result<AdminLoginVO> result = adminController.login(loginDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
        assertEquals("admin", result.getData().getUserName());
        assertEquals("管理员", result.getData().getName());
        assertEquals("adminAccessToken", result.getData().getToken());
        assertEquals("adminRefreshToken", result.getData().getRefreshToken());
        verify(adminService).login(any(AdminLoginDTO.class));
        verify(tokenService).generateAdminTokens(1L);
    }

    @Test
    @DisplayName("管理员退出登录 - 成功")
    void logout_success() {
        // 1. 准备数据
        String accessToken = "adminAccessToken";
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "adminRefreshToken");
        doNothing().when(tokenService).adminLogout(anyString(), anyString());

        // 2. 调用方法
        Result<Void> result = adminController.logout(accessToken, request);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(tokenService).adminLogout(accessToken, "adminRefreshToken");
    }

    @Test
    @DisplayName("新增管理员 - 成功")
    void save_success() throws Exception {
        // 1. 准备数据
        AdminDTO adminDTO = new AdminDTO();
        when(adminService.save(any(AdminDTO.class))).thenReturn(Result.success());

        // 2. 调用方法
        Result result = adminController.save(adminDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(adminService).save(any(AdminDTO.class));
    }

    @Test
    @DisplayName("分页查询管理员 - 成功")
    void page_success() {
        // 1. 准备数据
        AdminPageQueryDTO queryDTO = new AdminPageQueryDTO();
        PageResult pageResult = new PageResult(10L, new ArrayList<>());
        when(adminService.page(any(AdminPageQueryDTO.class))).thenReturn(pageResult);

        // 2. 调用方法
        Result<PageResult> result = adminController.page(queryDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(adminService).page(any(AdminPageQueryDTO.class));
    }

    @Test
    @DisplayName("启用或禁用管理员账号 - 成功")
    void startOrStop_success() {
        // 1. 准备数据
        doNothing().when(adminService).startOrStop(anyInt(), anyLong());

        // 2. 调用方法
        Result result = adminController.startOrStop(1, 1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(adminService).startOrStop(1, 1L);
    }

    @Test
    @DisplayName("根据管理员ID查询管理员 - 成功")
    void getById_success() {
        // 1. 准备数据
        Admin admin = new Admin();
        admin.setId(1L);
        when(adminService.getById(1L)).thenReturn(admin);

        // 2. 调用方法
        Result<Admin> result = adminController.getById(1L);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        verify(adminService).getById(1L);
    }

    @Test
    @DisplayName("编辑管理员信息 - 成功")
    void update_success() {
        // 1. 准备数据
        AdminDTO adminDTO = new AdminDTO();
        doNothing().when(adminService).update(any(AdminDTO.class));

        // 2. 调用方法
        Result result = adminController.update(adminDTO);

        // 3. 验证结果
        assertEquals(1, result.getCode());
        verify(adminService).update(any(AdminDTO.class));
    }
}