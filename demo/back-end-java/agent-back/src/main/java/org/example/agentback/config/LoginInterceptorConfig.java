package org.example.agentback.config;

import jakarta.annotation.Resource;
import org.example.agentback.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoginInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/login")
                .excludePathPatterns("/register")
                .excludePathPatterns("/SendOTP")
                .excludePathPatterns("/LoginByEmail")
                .excludePathPatterns("/ai/stream-chat");
    }
}
