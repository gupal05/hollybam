package com.hollybam.hollybam.controller.authController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.IF_SignupService;
import com.hollybam.hollybam.services.SignupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class SignupController {
    @Autowired
    private SignupService signupService;

    //회원가입 페이지 이동 메서드
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("memberDto", new MemberDto());
        return "/auth/signup";
    }

    @PostMapping("/dupCheckId")
    @ResponseBody
    public Map<String, Object> dupCheckId(@RequestParam String memberId) {
        int isIdCount = signupService.dupCheckId(memberId);
        boolean is = false;
        if(isIdCount > 0) {
            is = true;
        } else {
            is = false;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("duplicate", is);
        return result;
    }

    @PostMapping("/signupResult")
    @ResponseBody
    public Map<String, Object> signupResult(@ModelAttribute MemberDto memberDto) {
        System.out.println(memberDto);
        int result = signupService.signup(memberDto);
        Map<String, Object> signupResult = new HashMap<>();
        signupResult.put("signupResult", result > 0);
        return signupResult;
    }
}
