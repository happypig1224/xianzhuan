package com.xianzhuan.controller.user;

import com.xianzhuan.dto.UserLoginDTO;
import com.xianzhuan.entity.User;
import com.xianzhuan.properties.JwtProperties;
import com.xianzhuan.result.Result;
import com.xianzhuan.service.TokenService;
import com.xianzhuan.service.UserService;
import com.xianzhuan.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user/user")
@Tag(name = "用户相关接口")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public Result<UserLoginVO> wxLogin(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信登录:{}",userLoginDTO.getCode());
        User user = userService.wxLogin(userLoginDTO);
        
        Map<String, String> tokens = tokenService.generateUserTokens(user.getId());
        
        UserLoginVO userLoginVO=UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(tokens.get("accessToken"))
                .refreshToken(tokens.get("refreshToken"))
                .build();
        return Result.success(userLoginVO);
    }
}
