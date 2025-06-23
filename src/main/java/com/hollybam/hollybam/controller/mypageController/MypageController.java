package com.hollybam.hollybam.controller.mypageController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.WishlistDto;
import com.hollybam.hollybam.services.MypageService;
import com.hollybam.hollybam.services.IF_WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mypage")
public class MypageController {
    @Autowired
    MypageService mypageService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private IF_WishlistService wishlistService;

    @GetMapping("")
    public String mypage(HttpSession session, Model model) {
        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            Integer memCode = member != null ? member.getMemberCode() : null;
            String guestUuid = (String) session.getAttribute("guest_uuid");

            // 위시리스트 데이터 가져오기 (최신 3개만)
            List<WishlistDto> recentWishlist;
            int totalWishlistCount = 0;

            if (memCode != null) {
                // 회원
                List<WishlistDto> allWishlist = wishlistService.getMemberWishlist(memCode);
                recentWishlist = allWishlist.stream().limit(3).toList();
                totalWishlistCount = wishlistService.getMemberWishlistCount(memCode);
            } else if (guestUuid != null) {
                // 비회원
                Integer guestCode = wishlistService.getGuestCodeByUuid(guestUuid);
                if (guestCode != null) {
                    List<WishlistDto> allWishlist = wishlistService.getGuestWishlist(guestCode);
                    recentWishlist = allWishlist.stream().limit(3).toList();
                    totalWishlistCount = wishlistService.getGuestWishlistCount(guestCode);
                } else {
                    recentWishlist = List.of();
                }
            } else {
                recentWishlist = List.of();
            }

            // 모델에 데이터 추가
            model.addAttribute("recentWishlist", recentWishlist);
            model.addAttribute("totalWishlistCount", totalWishlistCount);
            model.addAttribute("isLoggedIn", memCode != null);

            return "mypage/mypage";

        } catch (Exception e) {
            e.printStackTrace();
            // 에러 발생 시에도 빈 리스트로 처리
            model.addAttribute("recentWishlist", List.of());
            model.addAttribute("totalWishlistCount", 0);
            return "mypage/mypage";
        }
    }

    @GetMapping("/orders")
    public String orders(){ return "mypage/orderList"; }

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

    @GetMapping("/coupons")
    public String couponPage(HttpSession session){
        return "mypage/coupons";
    }
}