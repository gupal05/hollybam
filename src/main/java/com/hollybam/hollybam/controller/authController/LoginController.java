package com.hollybam.hollybam.controller.authController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.LoginService;
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
        System.out.println("Login Page");
        return "/auth/login";
    }

    @PostMapping("/loginResult")
    @ResponseBody
    public Map<String, Object> loginResult(@ModelAttribute MemberDto memberDto) {
        Map<String, Object> result = new HashMap<>();
        if(loginService.login(memberDto)) {
            session.removeAttribute("temp");
            memberDto = loginService.getMemberInfo(memberDto.getMemberId());
            System.out.println(memberDto);
            session.setAttribute("member", memberDto);
            result.put("loginResult", true);
        } else {
            result.put("loginResult", false);
        }

        return result;
    }

}
