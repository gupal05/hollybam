package com.hollybam.hollybam.controller.mypageController;

import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.dto.WishlistDto;
import com.hollybam.hollybam.services.CouponService;
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

import java.text.NumberFormat;
import java.util.*;

@Controller
@RequestMapping("/mypage")
public class MypageController {
    @Autowired
    MypageService mypageService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private IF_WishlistService wishlistService;

    @Autowired
    private CouponService couponService;

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
            int couponCount = 0;
            if (memCode != null) {
                // 회원
                List<WishlistDto> allWishlist = wishlistService.getMemberWishlist(memCode);
                recentWishlist = allWishlist.stream().limit(3).toList();
                totalWishlistCount = wishlistService.getMemberWishlistCount(memCode);
                couponCount = couponService.selectCouponCount(memCode);
                int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
                String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
                model.addAttribute("totalPoint", totalPoint);
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
            model.addAttribute("couponCount", couponCount);
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
    public String orders(Model model, HttpSession session) {
        int couponCount = 0;
        if(session.getAttribute("member") != null) {
            MemberDto member = (MemberDto) session.getAttribute("member");
            couponCount = couponService.selectCouponCount(member.getMemberCode());
            int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
            String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
            model.addAttribute("totalPoint", totalPoint);
            model.addAttribute("couponCount", couponCount);
        }
        return "mypage/orderList";
    }

    @GetMapping("/profile/edit")
    public ModelAndView profileEdit(ModelAndView mav, HttpSession session){
        int couponCount = 0;
        MemberDto member = (MemberDto)session.getAttribute("member");
        String phone = member.getMemberPhone().replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        couponCount = couponService.selectCouponCount(member.getMemberCode());
        int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
        String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
        mav.addObject("totalPoint", totalPoint);
        mav.addObject("couponCount", couponCount);
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
    public String couponPage(HttpSession session, Model model) {
        int couponPossibleCount = 0;
        int couponCount = 0;
        int useCouponCount = 0;
        int expirationCouponCount = 0;
        List<Map<String, Object>> couponList = new ArrayList<>();

        if(session.getAttribute("member") != null) {
            MemberDto member = (MemberDto) session.getAttribute("member");
            int memCode = member.getMemberCode();

            // 쿠폰 통계 조회
            couponCount = couponService.selectTotalCouponCount(memCode);
            couponPossibleCount = couponService.selectCouponCount(memCode);
            useCouponCount = couponService.selectUsedCouponCount(memCode);
            expirationCouponCount = couponService.expirationCouponCount(memCode);

            // 전체 쿠폰 목록 조회
            couponList = couponService.selectMemberCouponList(memCode, "all");

            // 디버깅: 실제 Map 키 확인
            if (!couponList.isEmpty()) {
                System.out.println("=== 쿠폰 데이터 키 확인 ===");
                Map<String, Object> firstCoupon = couponList.get(0);
                System.out.println("Available keys: " + firstCoupon.keySet());
                System.out.println("Sample data: " + firstCoupon);
            }

            int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
            String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);

            model.addAttribute("totalPoint", totalPoint);
            model.addAttribute("couponCount", couponCount);
            model.addAttribute("couponPossibleCount", couponPossibleCount);
            model.addAttribute("useCouponCount", useCouponCount);
            model.addAttribute("expirationCouponCount", expirationCouponCount);
            model.addAttribute("couponList", couponList);

            System.out.println("Total coupons: " + couponCount);
            System.out.println("Used coupons: " + useCouponCount);
            System.out.println("Coupon list size: " + couponList.size());
            for (int i=0; i<couponList.size(); i++) {
                System.out.println(couponList.get(i));
            }
        }

        return "mypage/coupons";
    }

    /**
     * AJAX로 필터별 쿠폰 목록 조회
     */
    @GetMapping("/coupons/filter")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getCouponsByFilter(
            HttpSession session,
            @RequestParam(value = "status", defaultValue = "all") String status) {

        if(session.getAttribute("member") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MemberDto member = (MemberDto) session.getAttribute("member");
        List<Map<String, Object>> couponList = couponService.selectMemberCouponList(member.getMemberCode(), status);

        return ResponseEntity.ok(couponList);
    }

    /**
     * 쿠폰 사용 처리 (장바구니에서 사용할 때)
     */
    @PostMapping("/coupons/use")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> useCoupon(
            HttpSession session,
            @RequestParam("couponMemberCode") int couponMemberCode,
            @RequestParam(value = "orderCode", required = false) Integer orderCode) {

        Map<String, Object> result = new HashMap<>();

        if(session.getAttribute("member") == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        try {
            // 주문 코드가 없으면 임시로 0 설정 (장바구니에서 선택만 하는 경우)
            if(orderCode == null) {
                orderCode = 0;
            }

            boolean success = couponService.useCoupon(couponMemberCode, orderCode);

            if(success) {
                result.put("success", true);
                result.put("message", "쿠폰이 선택되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "쿠폰 사용에 실패했습니다.");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/points")
    public String pointsPage(HttpSession session, Model model) {
        MemberDto member = (MemberDto) session.getAttribute("member");
        int couponPossibleCount = 0;
        couponPossibleCount = couponService.selectCouponCount(member.getMemberCode());
        int totalPoints = mypageService.selectMemberPoint(member.getMemberCode());
        int addPoints = mypageService.selectMemberAddPoint(member.getMemberCode());
        int usePoints = mypageService.selectMemberUsePoint(member.getMemberCode());
        String totalPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(totalPoints);
        String addPoint = NumberFormat.getNumberInstance(Locale.KOREA).format(addPoints);
        String usePoint = NumberFormat.getNumberInstance(Locale.KOREA).format(usePoints);

        model.addAttribute("couponCount",  couponPossibleCount);
        model.addAttribute("totalPoint", totalPoint);
        model.addAttribute("addPoint", addPoint);
        model.addAttribute("usePoint", usePoint);
        return "mypage/points";
    }
}