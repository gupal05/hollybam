package com.hollybam.hollybam.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession();
        Object guest = session.getAttribute("guest");
        Object member = session.getAttribute("member");
        Object temp = session.getAttribute("temp");

        if (guest != null || member != null || temp != null) {
            return true; // 통과
        }

        // FlashAttribute는 직접 넣을 수 없으므로 쿼리 파라미터로 처리하거나 세션 사용
        request.getSession().setAttribute("message", "성인인증이 필요한 페이지 입니다.");
        response.sendRedirect(request.getContextPath() + "/");
        return false;
    }
}
