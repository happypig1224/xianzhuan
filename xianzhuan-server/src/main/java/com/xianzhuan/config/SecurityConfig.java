package com.xianzhuan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置类
 * 由于项目使用自定义 JWT 认证，因此禁用 Spring Security 的默认认证机制
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（因为使用 JWT 无状态认证）
            .csrf(csrf -> csrf.disable())
            // 禁用 HTTP Basic 认证
            .httpBasic(httpBasic -> httpBasic.disable())
            // 禁用表单登录
            .formLogin(formLogin -> formLogin.disable())
            // 禁用登出
            .logout(logout -> logout.disable())
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许所有请求访问，具体权限控制由自定义拦截器处理
                .anyRequest().permitAll()
            )
            // 禁用 Session（使用 JWT 无状态认证）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}