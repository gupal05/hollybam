package com.hollybam.hollybam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF는 나중에 활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",             // intro (성인 인증)
                                "/signup",       // 회원가입 페이지
                                "/signupResult", // 회원가입 완료 페이지
                                "loading",
                                "/css/**", "/js/**", "/images/**" // 정적 리소스
                        ).permitAll()
                        .anyRequest().permitAll() // ✅ 모든 요청 일단 허용 (로그인 구현 전이므로)
                )
                .formLogin(form -> form.disable()); // ✅ 기본 로그인 화면 비활성화

        return http.build();
    }

    @Bean
    public HttpFirewall customHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash(true); // URL 인코딩된 슬래시 허용
        firewall.setAllowSemicolon(true);             // 필요에 따라 세미콜론도 허용
        firewall.setAllowBackSlash(true);             // 백슬래시 허용 (선택)
        return firewall;
    }

}



