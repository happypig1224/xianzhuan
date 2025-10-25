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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

/**
 * @author Wu, Hui Ming
 * @version 1.0
 * @School Suihua University
 */
@RestController
@RequestMapping("/admin/admin")
@Slf4j
@Tag(name = "Admin:管理员相关接口")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     *
     * @param adminLoginDTO
     * @return
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) {
        // 步骤1: 记录登录日志
        log.info("管理员登录：{}", adminLoginDTO);

        // 步骤2: 调用服务层验证登录
        Admin admin = adminService.login(adminLoginDTO);

        // 步骤3: 生成双Token
        Map<String, String> tokens = tokenService.generateAdminTokens(admin.getId());

        // 步骤4: 构建返回VO
        AdminLoginVO adminLoginVO = AdminLoginVO.builder()
                .id(admin.getId())
                .userName(admin.getUsername())
                .name(admin.getName())
                .token(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .build();

        return Result.success(adminLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(name = "token", required = false) String accessToken,
                                @RequestBody(required = false) Map<String, String> request) {
        String refreshToken = (request != null) ? request.get("refreshToken") : null;
        tokenService.adminLogout(accessToken, refreshToken);
        return Result.success();
    }

    @PostMapping
    @Operation(summary = "新增管理员")
    public Result save(@RequestBody AdminDTO adminDTO) throws SQLIntegrityConstraintViolationException {
        return adminService.save(adminDTO);
    }


    @Operation(summary = "分页查询管理员")
    @GetMapping("/page")
    public Result<PageResult> page(AdminPageQueryDTO pageQueryDTO) {
        PageResult pageResult = adminService.page(pageQueryDTO);
        return Result.success(pageResult
        );
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启用或禁用管理员账号")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用/禁用的账号是:{},{}", status, id);
        adminService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("{id}")
    @Operation(summary = "根据管理员ID查询管理员")
    public Result<Admin> getById(@PathVariable Long id) {
        Admin admin = adminService.getById(id);
        return Result.success(admin);
    }
    @PutMapping
    @Operation(summary = "编辑管理员信息")
    public Result update(@RequestBody AdminDTO adminDTO){
         adminService.update(adminDTO);
         return Result.success();
    }
}