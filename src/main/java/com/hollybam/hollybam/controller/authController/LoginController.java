package com.hollybam.hollybam.controller.authController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.LoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class LoginController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private HttpSession session;

    @PostMapping("/login")
    public String loginPage() {
        System.out.println("Login Page");
        return "/auth/login";
    }

    @PostMapping("/loginResult")
    @ResponseBody
    public Map<String, Object> loginResult(@ModelAttribute MemberDto memberDto) {
        System.out.println(memberDto);
        Map<String, Object> result = new HashMap<>();
        if(loginService.login(memberDto) > 0){
            result.put("loginResult", true);
            session.setAttribute("memberInfo", loginService.getMemberInfo(memberDto.getMemberId()));
        } else {
            result.put("loginResult", false);
        }

        return result;
    }

}
