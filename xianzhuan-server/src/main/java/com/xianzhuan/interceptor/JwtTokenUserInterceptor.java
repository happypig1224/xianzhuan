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
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private TokenService tokenService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        BaseContext.removeCurrentId();
    }

    /**
     * 校验 jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String userIdHeader = request.getHeader("userId");
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            log.error("用户id为空");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"用户id为空\"}");
            return false;
        }
        BaseContext.setCurrentId(Long.valueOf(userIdHeader));
        return true;

       /* String token = request.getHeader(jwtProperties.getUserTokenName());
        if (token == null) {
            log.error("用户token为空");
            response.setStatus(401);
            return false;
        }
        
        if (tokenService.isTokenBlacklisted(token)) {
            log.error("用户token已在黑名单中");
            response.setStatus(401);
            return false;
        }
        
        try {
            log.info("校验token{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            
            String tokenType = claims.get(JwtUtil.TOKEN_TYPE_KEY, String.class);
            if (!JwtUtil.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                log.error("Token类型错误，不是AccessToken");
                response.setStatus(401);
                return false;
            }
            
            Long userId = Long.valueOf(claims.get("userId").toString());
            log.info("当前用户 id{}", userId);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception e) {
            log.error("用户token校验失败", e);
            response.setStatus(401);
            return false;
        }*/
    }
}