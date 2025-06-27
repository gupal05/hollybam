package com.hollybam.hollybam.controller.adminController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.LoginService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/auth")
public class AdminAuthController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> login(@ModelAttribute MemberDto member, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("관리자 로그인 시도 - 사용자: {}", member.getMemberId());

            if(loginService.login(member)) {
                member = loginService.getMemberInfo(member.getMemberId());

                if(!"admin".equals(member.getMemberRole())) {
                    result.put("loginResult", false);
                    result.put("message", "접근 권한이 없는 계정입니다.");
                    log.warn("권한 없는 로그인 시도 - 사용자: {}, 역할: {}", member.getMemberId(), member.getMemberRole());
                } else {
                    session.setAttribute("member", member);
                    result.put("loginResult", true);
                    result.put("message", "로그인 성공");
                    log.info("관리자 로그인 성공 - 사용자: {}", member.getMemberId());
                }
            } else {
                result.put("loginResult", false);
                result.put("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
                log.warn("로그인 실패 - 사용자: {}", member.getMemberId());
            }
        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생 - 사용자: {}", member.getMemberId(), e);
            result.put("loginResult", false);
            result.put("message", "로그인 처리 중 오류가 발생했습니다.");
        }

        return result;
    }
}
