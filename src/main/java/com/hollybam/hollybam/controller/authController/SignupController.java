package com.hollybam.hollybam.controller.authController;

import com.hollybam.hollybam.dto.GuestDto;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.IF_SignupService;
import com.hollybam.hollybam.services.SignupService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class SignupController {
    @Autowired
    private SignupService signupService;

    //회원가입 페이지 이동 메서드
    @GetMapping("/signup")
    public String signup(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        GuestDto guestDto = (GuestDto) session.getAttribute("guest");
        if(signupService.isRecodeSignup(guestDto.getGuestDi()) > 0) {
            redirectAttributes.addFlashAttribute("recodeMessage", "가입하신 이력이 존재합니다.");
            return "redirect:/auth/login";
        } else {
            MemberDto guestInfo = new MemberDto();
            guestInfo.setMemberName(guestDto.getGuestName());
            guestInfo.setMemberPhone(guestDto.getGuestPhone());
            guestInfo.setMemberBirth(guestDto.getGuestBirth());
            guestInfo.setMemberGender(guestDto.getGuestGender());
            guestInfo.setDi(guestDto.getGuestDi());
            guestInfo.setAdultVerifiedAt(guestDto.getAdultVerifiedAt());
            guestInfo.setAdultVerified(guestDto.isAdultVerified());
            guestInfo.setMemberLoginType("web");
            model.addAttribute("guestInfo", guestInfo);
            model.addAttribute("memberDto", new MemberDto());
            return "auth/signup";
        }
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
        System.out.println("db : "+memberDto);
        int result = signupService.signup(memberDto);
        int memberCode = signupService.getMemberCode(memberDto.getMemberId());
        int couponCode = signupService.getSignupCouponCode();
        signupService.insertSignupCoupon(memberCode, couponCode);

        // get memberCode
        // get order Info
        // if uuid로 주문한 orderInfo 있으면 order 테이블 memberCode update
        GuestDto guestInfo = (GuestDto) session.getAttribute("guest");
        if(signupService.getGuestCartCount(guestInfo) > 0){
            signupService.updateGuestToMemberCart(memberCode, guestInfo.getGuestCode());
        }
        if(signupService.getGuestWishCount(guestInfo) > 0){
            signupService.updateGuestToMemberWishList(memberCode, guestInfo.getGuestCode());
        }
        if(signupService.getGuestOrderCount(guestInfo) > 0){
            signupService.updateGuestToMemberOrder(memberCode, guestInfo.getGuestCode());
        }
        if(signupService.getGuestInquiryCount(guestInfo) > 0){
            signupService.updateGuestToMemberInquiry(memberCode, guestInfo.getGuestCode());
        }
        signupService.deleteGuestByDi(guestInfo.getGuestDi());
        session.removeAttribute("guest");
        Map<String, Object> signupResult = new HashMap<>();
        signupResult.put("signupResult", result > 0);
        return signupResult;
    }

    @PostMapping("/mail")
    @ResponseBody
    public Map<String, Object> mail(@RequestParam String email, HttpSession session) {
        String message = signupService.mainAuth(email, session);
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("result", message.equals("인증번호가 전송 되었습니다."));
        return map;
    }

    @PostMapping("/mail/result")
    @ResponseBody
    public Map<String, Object> mainResult(@RequestParam String code, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        String mailCode = session.getAttribute("mailCode").toString();
        if(code.equals(mailCode)) {
            map.put("result", true);
            session.removeAttribute("mailCode");
        } else {
            map.put("result", false);
        }
        return map;
    }
}
