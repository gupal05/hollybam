package com.hollybam.hollybam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")            // 전체 경로 검사
                .excludePathPatterns("/", "/css/**", "/js/**", "/images/**", "/verify-cert", "/auth/loginResult", "/order", "/cart", "/admin/**"); // 예외 처리
    }
}

