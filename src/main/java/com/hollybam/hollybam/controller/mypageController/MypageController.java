package com.hollybam.hollybam.controller.mypageController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.MypageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/mypage")
public class MypageController {
    @Autowired
    MypageService mypageService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("")
    public String mypage(){ return "mypage/mypage"; }

    @GetMapping("/orders")
    public String orders(){ return "mypage/orderList"; }

    @GetMapping("/wishlist")
    public String wishlist(){ return "mypage/wishlist"; }

    @GetMapping("/profile/edit")
    public ModelAndView profileEdit(ModelAndView mav, HttpSession session){
        MemberDto member = (MemberDto)session.getAttribute("member");
        String phone = member.getMemberPhone().replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        mav.addObject("phone", phone);
        mav.setViewName("mypage/change-info");
        return mav;
    }

    @PostMapping("/member/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> memberUpdate(@ModelAttribute MemberDto memberDto, HttpSession session){
        System.out.println(memberDto);
        Map<String, Object> map = new HashMap<>();
        try {
            MemberDto member = (MemberDto)session.getAttribute("member");
            member.setMemberPw(passwordEncoder.encode(memberDto.getMemberPw()));
            mypageService.updateMember(member);
            map.put("success", true);
            map.put("message", "비밀번호가 성공적으로 수정되었습니다.");
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            map.put("success", false);
            map.put("message", "비밀번호 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }
    }
}
