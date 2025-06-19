package com.hollybam.hollybam.controller.authController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.IF_SignupService;
import com.hollybam.hollybam.services.SignupService;
import jakarta.servlet.http.HttpSession;
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
    public String signup(Model model, HttpSession session) {
        MemberDto guestInfo = new MemberDto();
        guestInfo = signupService.getGuestInfo((String) session.getAttribute("guest_uuid"));
        model.addAttribute("guestInfo", guestInfo);
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
    public Map<String, Object> signupResult(@ModelAttribute MemberDto memberDto, HttpSession session) {
        int result = signupService.signup(memberDto);
        // get memberCode
        // get order Info
        // if uuid로 주문한 orderInfo 있으면 order 테이블 memberCode update
        signupService.deleteGuestByUuid((String) session.getAttribute("guest_uuid"));
        session.removeAttribute("guest_uuid");
        session.setAttribute("temp", "temp");
        Map<String, Object> signupResult = new HashMap<>();
        signupResult.put("signupResult", result > 0);
        return signupResult;
    }
}
