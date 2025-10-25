package com.xianzhuan.interceptor;

import com.xianzhuan.constant.JwtClaimsConstant;
import com.xianzhuan.context.BaseContext;
import com.xianzhuan.properties.JwtProperties;
import com.xianzhuan.service.TokenService;
import com.xianzhuan.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TokenService tokenService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        BaseContext.removeCurrentId();
    }

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String userIdHeader = request.getHeader("userId");
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            log.error("用户id为空");
            return false;
        }
        BaseContext.setCurrentId(Long.valueOf(userIdHeader));
        return true;
        /*String token = request.getHeader(jwtProperties.getAdminTokenName());
        if (token == null) {
            log.error("管理员token为空");
            response.setStatus(401);
            return false;
        }
        
        if (tokenService.isTokenBlacklisted(token)) {
            log.error("管理员token已在黑名单中");
            response.setStatus(401);
            return false;
        }
        
        try{
            log.info("校验token{}",token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            
            String tokenType = claims.get(JwtUtil.TOKEN_TYPE_KEY, String.class);
            if (!JwtUtil.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                log.error("Token类型错误，不是AccessToken");
                response.setStatus(401);
                return false;
            }
            
            Long empId = Long.valueOf(claims.get("userId").toString());
            log.info("当前用户id{}",empId);
            BaseContext.setCurrentId(empId);
            return true;
        }catch (Exception e){
            log.error("管理员token校验失败", e);
            response.setStatus(401);
            return false;
        }*/
    }
}
