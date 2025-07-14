package com.hollybam.hollybam.controller.authController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private HttpSession session;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/loginResult")
    @ResponseBody
    public Map<String, Object> loginResult(@ModelAttribute MemberDto memberDto) {
        Map<String, Object> result = new HashMap<>();
        if(loginService.login(memberDto)) {
            if(session.getAttribute("temp") != null) {
                session.removeAttribute("temp");
            }
            if(session.getAttribute("guest") != null) {
                session.removeAttribute("guest");
            }
            memberDto = loginService.getMemberInfo(memberDto.getMemberId());
            session.setAttribute("member", memberDto);
            result.put("loginResult", true);
        } else {
            result.put("loginResult", false);
        }

        return result;
    }

    @PostMapping("/verify-password")
    @ResponseBody
    public Map<String, Object> verifyPassword(@ModelAttribute MemberDto memberDto) {
        MemberDto member = (MemberDto) session.getAttribute("member");
        member.setMemberPw(memberDto.getMemberPw());
        Map<String, Object> result = new HashMap<>();
        if(loginService.login(member)) {
            result.put("valid", loginService.login(member));
        } else {
            result.put("valid", loginService.login(member));
        }
        return result;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        session.invalidate(); // 세션 종료

        // JSESSIONID 쿠키 삭제
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);

        // 캐시 방지
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        return "redirect:/";
    }

}
