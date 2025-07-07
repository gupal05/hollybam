// 옵션 1: OrderController에 결제 API들을 모두 추가하는 방법
package com.hollybam.hollybam.controller.orderController;

import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.CouponService;
import com.hollybam.hollybam.services.DiscountService;
import com.hollybam.hollybam.services.IF_PaymentService;
import com.hollybam.hollybam.services.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
@Slf4j
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private ProductService productService;
    @Autowired
    private IF_PaymentService paymentService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private DiscountService discountService;

    @PostMapping("/checkout")
    public ModelAndView introPage(@RequestParam("cartCodes") List<String> cartCodes,
                                  HttpSession session, ModelAndView mav) {
        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            // String을 Integer로 변환
            List<Integer> cartCodeList = new ArrayList<>();
            for (String code : cartCodes) {
                try {
                    cartCodeList.add(Integer.parseInt(code));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid cart code: " + code);
                }
            }

            // 장바구니 상품 정보 조회 및 주문 정보 계산
            PaymentRequestDto paymentInfo = paymentService.calculateOrderFromCart(
                    cartCodeList,
                    member != null ? member.getMemberCode() : null,
                    guest.getGuestCode());

            // 장바구니 상품 상세 정보 조회 (상품명, 이미지, 옵션 등)
            List<CartDto> cartItems = getCartItemsWithDetails(cartCodeList);
            List<CouponDto> couponList = new ArrayList<>();
            try {
                if(member != null) {
                    System.out.println(member.getMemberCode());
                    couponList = couponService.getUsePossibleCoupon(member.getMemberCode());
                    System.out.println(couponList.size());
                    System.out.println(couponList);
                }
                for (CouponDto couponDto : couponList) {
                    System.out.println(couponDto);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ====================================================================================비회원 구현

            // ModelAndView에 데이터 추가
            mav.addObject("member", member);
            mav.addObject("paymentInfo", paymentInfo);
            mav.addObject("cartItems", cartItems);
            mav.addObject("cartCodes", cartCodeList);
            mav.addObject("couponList", couponList);
            mav.setViewName("order");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "주문 페이지를 불러올 수 없습니다: " + e.getMessage());
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 장바구니 상품 상세 정보 조회
     */
    private List<CartDto> getCartItemsWithDetails(List<Integer> cartCodes) {
        try {
            return paymentService.getCartItemsWithDetails(cartCodes);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @PostMapping("/discount/validate")
    public ResponseEntity<Map<String, Object>> validateDiscountCode(
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            String discountId = (String) request.get("discountId");
            Long orderAmount = Long.valueOf(request.get("orderAmount").toString());

            log.info("할인코드 검증 요청: discountId={}, orderAmount={}", discountId, orderAmount);

            // 할인코드 검증
            Map<String, Object> validationResult = discountService.validateDiscountCode(discountId, orderAmount);

            response.put("success", true);
            response.put("data", validationResult.get("discountInfo"));
            response.put("discountAmount", validationResult.get("discountAmount"));
            response.put("message", "할인코드가 성공적으로 적용되었습니다.");

            log.info("할인코드 검증 성공: discountId={}, discountAmount={}",
                    discountId, validationResult.get("discountAmount"));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (RuntimeException e) {
            log.warn("할인코드 검증 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("할인코드 검증 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "할인코드 확인 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

}