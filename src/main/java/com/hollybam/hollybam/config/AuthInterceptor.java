package com.hollybam.hollybam.config;

import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.MemberDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        log.info("🔍 AuthInterceptor 체크: {}", requestURI);

        // 허용 경로 체크
        if (isAllowedPath(requestURI)) {
            log.info("✅ 허용 경로 통과: {}", requestURI);
            return true;
        }

        HttpSession session = request.getSession();
        MemberDto member = (MemberDto) session.getAttribute("member");
        GuestDto guest = (GuestDto) session.getAttribute("guest");

        log.info("🔍 세션 체크 - member: {}, guest: {}",
                (member != null ? "존재" : "없음"),
                (guest != null ? "존재" : "없음"));

        // 관리자 페이지 체크
//        if (requestURI.startsWith("/admin")) {
//            if (member == null || !"admin".equals(member.getMemberRole())) {
//                log.warn("🚨 관리자 페이지 접근 차단: {}", requestURI);
//                response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
//                return false;
//            }
//            log.info("✅ 관리자 페이지 접근 허용: {}", requestURI);
//            return true;
//        }

        // 인증되지 않은 사용자 체크
        if (member == null && guest == null) {
            log.warn("🚨 인증 없는 접근 차단: {} → intro로 리다이렉트", requestURI);
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        // 회원 체크
        if (member != null) {
            log.info("✅ 회원 접근 허용: {} (회원: {})", requestURI, member.getMemberId());
            return true;
        }

        // 비회원 성인인증 체크
        if (guest != null && Boolean.TRUE.equals(guest.isAdultVerified())) {
            log.info("✅ 비회원 접근 허용: {} (성인인증 완료)", requestURI);
            return true;
        }

        // 비회원이지만 성인인증 안 된 경우
        log.warn("🚨 비회원 성인인증 필요: {} → intro로 리다이렉트", requestURI);
        response.sendRedirect(request.getContextPath() + "/");
        return false;
    }

    // 🔥 수정된 허용 경로 체크 메소드
    private boolean isAllowedPath(String requestURI) {
        // 정확한 경로 매칭
        if ("/".equals(requestURI) || "/loading".equals(requestURI)) {
            return true;
        }

        // startsWith로 체크하는 경로들
        String[] prefixPaths = {
                "/css/", "/js/", "/images/", "/favicon.ico",
                "/auth/", "/nice/", "/naver/", "/google/", "/error"
        };

        return Arrays.stream(prefixPaths).anyMatch(requestURI::startsWith);
    }
}

