package com.xianzhuan.controller;

import com.xianzhuan.result.Result;
import com.xianzhuan.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class TokenController {
    
    @Autowired
    private TokenService tokenService;
    
    /**
     * 用户端刷新Token
     */
    @PostMapping("/user/refresh-token")
    public Result<Map<String, String>> refreshUserToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        log.info("用户端刷新Token");
        Map<String, String> tokens = tokenService.refreshUserToken(refreshToken);
        return Result.success(tokens);
    }
    
    /**
     * 管理端刷新Token
     */
    @PostMapping("/admin/refresh-token")
    public Result<Map<String, String>> refreshAdminToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        log.info("管理端刷新Token");
        Map<String, String> tokens = tokenService.refreshAdminToken(refreshToken);
        return Result.success(tokens);
    }
    
    /**
     * 用户端登出
     */
    @PostMapping("/user/logout")
    public Result<Void> userLogout(@RequestHeader(name = "authentication", required = false) String accessToken,
                                    @RequestBody(required = false) Map<String, String> request) {
        String refreshToken = (request != null) ? request.get("refreshToken") : null;
        log.info("用户端登出");
        tokenService.userLogout(accessToken, refreshToken);
        return Result.success();
    }
    
    /**
     * 管理端登出
     */
    @PostMapping("/admin/logout")
    public Result<Void> adminLogout(@RequestHeader(name = "token", required = false) String accessToken,
                                    @RequestBody(required = false) Map<String, String> request) {
        String refreshToken = (request != null) ? request.get("refreshToken") : null;
        log.info("管理端登出");
        tokenService.adminLogout(accessToken, refreshToken);
        return Result.success();
    }
}
