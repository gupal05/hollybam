package com.hollybam.hollybam.controller;

import com.hollybam.hollybam.dto.CouponMasterDto;
import com.hollybam.hollybam.dto.MemberCouponDto;
import com.hollybam.hollybam.dto.MemberDto;
import com.hollybam.hollybam.services.IF_CouponService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 쿠폰 컨트롤러
 * 쿠폰 관련 요청 처리
 */
@Controller
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private IF_CouponService couponService;

    // ===============================
    // 사용자 쿠폰 기능
    // ===============================

    /**
     * 발급 가능한 쿠폰 목록 페이지
     */
    @GetMapping("/available")
    public ModelAndView availableCoupons() {
        ModelAndView mav = new ModelAndView();

        try {
            List<CouponMasterDto> issuableCoupons = couponService.getIssuableCouponList();

            mav.addObject("coupons", issuableCoupons);
            mav.setViewName("coupon/availableCoupons");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "쿠폰 목록을 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 쿠폰 발급받기 (AJAX)
     */
    @PostMapping("/issue")
    @ResponseBody
    public Map<String, Object> issueCoupon(@RequestParam Integer couponMasterCode, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            boolean result = couponService.issueCouponByMember(couponMasterCode, member.getMemberCode());

            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰이 발급되었습니다!");
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 발급에 실패했습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 장바구니에서 사용 가능한 쿠폰 목록 조회 (AJAX)
     */
    @PostMapping("/usable-for-cart")
    @ResponseBody
    public Map<String, Object> getUsableCouponsForCart(@RequestParam List<Integer> productCodes, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            List<MemberCouponDto> usableCoupons = couponService.getUsableCouponsForCart(member.getMemberCode(), productCodes);

            response.put("success", true);
            response.put("coupons", usableCoupons);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "쿠폰 목록을 불러올 수 없습니다.");
        }

        return response;
    }

    /**
     * 쿠폰 할인 금액 계산 (AJAX)
     */
    @PostMapping("/calculate-discount")
    @ResponseBody
    public Map<String, Object> calculateCouponDiscount(@RequestParam String couponCode,
                                                       @RequestParam List<Integer> productCodes,
                                                       @RequestParam Integer orderAmount,
                                                       HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return response;
            }

            // 쿠폰 사용 가능 여부 검증
            boolean isValid = couponService.validateCouponUsage(couponCode, member.getMemberCode(), productCodes, orderAmount);

            if (isValid) {
                Integer discountAmount = couponService.calculateCouponDiscount(couponCode, member.getMemberCode(), productCodes, orderAmount);
                Integer finalAmount = orderAmount - discountAmount;

                response.put("success", true);
                response.put("discountAmount", discountAmount);
                response.put("finalAmount", finalAmount);
            } else {
                response.put("success", false);
                response.put("message", "사용할 수 없는 쿠폰입니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // ===============================
    // 마이페이지 쿠폰 관리
    // ===============================

    /**
     * 마이페이지 - 내 쿠폰함
     */
    @GetMapping("/my-coupons")
    public ModelAndView myCoupons(@RequestParam(value = "status", defaultValue = "ISSUED") String status,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size,
                                  HttpSession session) {
        ModelAndView mav = new ModelAndView();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                mav.setViewName("redirect:/auth/login");
                return mav;
            }

            List<MemberCouponDto> memberCoupons = couponService.getMemberCoupons(member.getMemberCode(), status, page, size);
            int totalCount = couponService.getMemberCouponCount(member.getMemberCode(), status);
            int totalPages = couponService.calculateTotalPages(totalCount, size);

            // 상태별 쿠폰 개수 조회
            int issuedCount = couponService.getMemberCouponCount(member.getMemberCode(), "ISSUED");
            int usedCount = couponService.getMemberCouponCount(member.getMemberCode(), "USED");
            int expiredCount = couponService.getMemberCouponCount(member.getMemberCode(), "EXPIRED");

            mav.addObject("memberCoupons", memberCoupons);
            mav.addObject("currentStatus", status);
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", totalPages);
            mav.addObject("totalCount", totalCount);
            mav.addObject("pageSize", size);
            mav.addObject("issuedCount", issuedCount);
            mav.addObject("usedCount", usedCount);
            mav.addObject("expiredCount", expiredCount);

            mav.setViewName("mypage/coupons");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "쿠폰 목록을 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 마이페이지 - 쿠폰 사용 내역
     */
    @GetMapping("/usage-history")
    public ModelAndView couponUsageHistory(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                           HttpSession session) {
        ModelAndView mav = new ModelAndView();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                mav.setViewName("redirect:/auth/login");
                return mav;
            }

            // 쿠폰 사용 내역 조회 (구현 필요)
            mav.addObject("usageHistory", null);
            mav.setViewName("mypage/couponUsageHistory");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "쿠폰 사용 내역을 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    // ===============================
    // 관리자 쿠폰 관리
    // ===============================

    /**
     * 관리자 - 쿠폰 목록
     */
    @GetMapping("/admin/list")
    public ModelAndView adminCouponList(@RequestParam(value = "page", defaultValue = "1") int page,
                                        @RequestParam(value = "size", defaultValue = "10") int size,
                                        HttpSession session) {
        ModelAndView mav = new ModelAndView();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !couponService.isAdminUser(member.getMemberCode())) {
                mav.addObject("error", "관리자만 접근할 수 있습니다.");
                mav.setViewName("error/error");
                return mav;
            }

            List<CouponMasterDto> couponMasters = couponService.getAllCouponMasterList(page, size);

            mav.addObject("couponMasters", couponMasters);
            mav.addObject("currentPage", page);
            mav.addObject("pageSize", size);
            mav.setViewName("admin/coupon/couponList");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "쿠폰 목록을 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 관리자 - 쿠폰 생성 페이지
     */
    @GetMapping("/admin/create")
    public ModelAndView createCouponForm(HttpSession session) {
        ModelAndView mav = new ModelAndView();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !couponService.isAdminUser(member.getMemberCode())) {
                mav.addObject("error", "관리자만 접근할 수 있습니다.");
                mav.setViewName("error/error");
                return mav;
            }

            mav.addObject("couponMaster", new CouponMasterDto());
            mav.setViewName("admin/coupon/createCoupon");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "페이지를 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 관리자 - 쿠폰 생성 처리
     */
    @PostMapping("/admin/create")
    @ResponseBody
    public Map<String, Object> createCoupon(@ModelAttribute CouponMasterDto couponMasterDto,
                                            @RequestParam(required = false) List<Integer> applicableProductCodes,
                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !couponService.isAdminUser(member.getMemberCode())) {
                response.put("success", false);
                response.put("message", "관리자만 접근할 수 있습니다.");
                return response;
            }

            boolean result = couponService.createCouponMaster(couponMasterDto, applicableProductCodes, member.getMemberCode());

            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰이 생성되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 생성에 실패했습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 관리자 - 회원에게 쿠폰 직접 발급
     */
    @PostMapping("/admin/issue")
    @ResponseBody
    public Map<String, Object> issueCouponToMember(@RequestParam Integer couponMasterCode,
                                                   @RequestParam Integer memCode,
                                                   HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !couponService.isAdminUser(member.getMemberCode())) {
                response.put("success", false);
                response.put("message", "관리자만 접근할 수 있습니다.");
                return response;
            }

            boolean result = couponService.issueCouponToMember(couponMasterCode, memCode, member.getMemberCode());

            if (result) {
                response.put("success", true);
                response.put("message", "쿠폰이 발급되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "쿠폰 발급에 실패했습니다.");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 관리자 - 대량 쿠폰 발급
     */
    @PostMapping("/admin/bulk-issue")
    @ResponseBody
    public Map<String, Object> bulkIssueCoupon(@RequestParam Integer couponMasterCode,
                                               @RequestParam List<Integer> memCodes,
                                               HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null || !couponService.isAdminUser(member.getMemberCode())) {
                response.put("success", false);
                response.put("message", "관리자만 접근할 수 있습니다.");
                return response;
            }

            int issuedCount = couponService.issueCouponToMultipleMembers(couponMasterCode, memCodes, member.getMemberCode());

            response.put("success", true);
            response.put("message", "총 " + issuedCount + "명에게 쿠폰을 발급했습니다.");
            response.put("issuedCount", issuedCount);
            response.put("totalRequested", memCodes.size());

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
}