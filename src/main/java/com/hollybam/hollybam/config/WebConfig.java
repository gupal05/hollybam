package com.hollybam.hollybam.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.ForwardedHeaderFilter;
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
                .excludePathPatterns(
                        // 🔥 최소한의 경로만 제외
                        "/robots.txt",
                        "/sitemap.xml",
                        "/",                    // intro 페이지
                        "/admin",
                        "/admin/auth/**",
                        "/loading",             // 로딩 페이지
                        "/css/**", "/js/**", "/images/**", "/favicon.ico", // 정적 리소스
                        "/auth/**",             // 인증 관련
                        "/nice/**",             // NICE 본인인증
                        "/naver/**",            // 네이버 로그인
                        "/login/**",
                        "/google/**",           // 구글 로그인
                        "/error",                // 에러 페이지
                        "/terms",
                        "/guide",
                        "/companyInfo"
                );
        // 🚨 중요: /main, /product, /cart, /order, /admin 등은 제외하지 않음!
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

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}