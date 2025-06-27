package com.hollybam.hollybam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")            // 전체 경로 검사
                .excludePathPatterns("/", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/verify-cert", "/auth/loginResult", "/order", "/cart", "/admin/**",
                        "/test/auth/**", "/auth/nice/**", "/test/**", "/nice/**", "/authResult",
                        "/error", "/authPopupCallbackView", "/authError", "/authPopupCallback"); // 예외 처리
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/test/nice/**")
                .allowedOrigins("https://nice.checkplus.co.kr")
                .allowedMethods("GET", "POST");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // favicon 처리
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

        // CSS 파일 처리
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

        // JavaScript 파일 처리
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

        // 이미지 파일 처리
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

        // 이미지 파일 처리
        registry.addResourceHandler("/testImage/**")
                .addResourceLocations("classpath:/static/testImage/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));

        // 기타 정적 리소스
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }
}