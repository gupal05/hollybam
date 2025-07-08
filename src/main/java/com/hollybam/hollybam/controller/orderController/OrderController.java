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

            // 로그인 상태 확인 - 둘 다 없으면 로그인 페이지로
            if (member == null && guest == null) {
                mav.setViewName("redirect:/auth/login");
                return mav;
            }

            // cartCodes 유효성 검사
            if (cartCodes == null || cartCodes.isEmpty()) {
                mav.setViewName("redirect:/cart");
                return mav;
            }

            // String을 Integer로 안전하게 변환
            List<Integer> cartCodeList = new ArrayList<>();
            for (String code : cartCodes) {
                try {
                    if (code != null && !code.trim().isEmpty()) {
                        cartCodeList.add(Integer.parseInt(code.trim()));
                    }
                } catch (NumberFormatException e) {
                    log.error("Invalid cart code: " + code, e);
                    // 잘못된 cartCode가 있어도 계속 진행
                }
            }

            // 유효한 cartCode가 없으면 장바구니로 리다이렉트
            if (cartCodeList.isEmpty()) {
                mav.setViewName("redirect:/cart");
                return mav;
            }

            // 안전한 null 체크로 주문 정보 계산
            Integer memberCode = member != null ? member.getMemberCode() : null;
            Integer guestCode = guest != null ? guest.getGuestCode() : null;

            PaymentRequestDto paymentInfo = paymentService.calculateOrderFromCart(
                    cartCodeList, memberCode, guestCode);

            // paymentInfo null 체크
            if (paymentInfo == null) {
                log.error("PaymentInfo is null");
                mav.setViewName("redirect:/cart");
                return mav;
            }

            // 장바구니 상품 상세 정보 조회
            List<CartDto> cartItems = getCartItemsWithDetails(cartCodeList);

            // cartItems null 또는 빈 리스트 체크
            if (cartItems == null || cartItems.isEmpty()) {
                log.error("CartItems is null or empty");
                mav.setViewName("redirect:/cart");
                return mav;
            }

            // 쿠폰 정보 조회 (회원만, 예외 발생해도 계속 진행)
            List<CouponDto> couponList = new ArrayList<>();
            try {
                if (member != null) {
                    couponList = couponService.getUsePossibleCoupon(member.getMemberCode());
                    if (couponList == null) {
                        couponList = new ArrayList<>();
                    }
                }
            } catch (Exception e) {
                log.error("쿠폰 조회 실패, 빈 리스트로 계속 진행", e);
                couponList = new ArrayList<>();
            }

            // ModelAndView에 데이터 추가 (모든 값이 null이 아님을 보장)
            mav.addObject("member", member);
            mav.addObject("paymentInfo", paymentInfo);
            mav.addObject("cartItems", cartItems);
            mav.addObject("cartCodes", cartCodeList);
            mav.addObject("couponList", couponList);
            mav.setViewName("order");

        } catch (Exception e) {
            log.error("주문 페이지 로드 중 예외 발생", e);
            // 어떤 예외가 발생해도 장바구니로 안전하게 리다이렉트
            mav.setViewName("redirect:/cart");
        }

        return mav;
    }

    /**
     * 장바구니 상품 상세 정보 조회 (안전한 버전)
     */
    private List<CartDto> getCartItemsWithDetails(List<Integer> cartCodes) {
        try {
            if (cartCodes == null || cartCodes.isEmpty()) {
                return new ArrayList<>();
            }

            List<CartDto> result = paymentService.getCartItemsWithDetails(cartCodes);
            return result != null ? result : new ArrayList<>();

        } catch (Exception e) {
            log.error("장바구니 상품 상세 정보 조회 실패", e);
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

    /**
     * 상품 상세 페이지에서 바로 구매
     */
    @PostMapping("/direct-purchase")
    public ModelAndView directPurchase(@RequestParam("productCode") int productCode,
                                       @RequestParam(value = "optionCode", required = false) Integer optionCode,
                                       @RequestParam("quantity") int quantity,
                                       HttpSession session, ModelAndView mav) {
        try {
            // 세션에서 사용자 정보 가져오기
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                mav.addObject("error", "로그인이 필요합니다.");
                mav.setViewName("redirect:/auth/login");
                return mav;
            }

            // 바로 구매할 상품 정보로 임시 주문 정보 생성
            PaymentRequestDto paymentInfo = paymentService.calculateDirectPurchase(
                    productCode, optionCode, quantity,
                    member != null ? member.getMemberCode() : null,
                    guest != null ? guest.getGuestCode() : null);

            // 바로 구매할 상품의 상세 정보 조회
            List<CartDto> cartItems = createDirectPurchaseItems(productCode, optionCode, quantity);

            // 쿠폰 정보 조회 (회원만)
            List<CouponDto> couponList = new ArrayList<>();
            try {
                if(member != null) {
                    couponList = couponService.getUsePossibleCoupon(member.getMemberCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ModelAndView에 데이터 추가 (기존 checkout과 동일한 구조)
            mav.addObject("member", member);
            mav.addObject("paymentInfo", paymentInfo);
            mav.addObject("cartItems", cartItems);
            mav.addObject("cartCodes", new ArrayList<Integer>()); // 빈 리스트 (바로구매는 cartCode 없음)
            mav.addObject("couponList", couponList);
            mav.addObject("isDirect", true); // 바로구매 구분용
            mav.setViewName("order");

        } catch (Exception e) {
            e.printStackTrace();
            mav.addObject("error", "주문 페이지를 불러올 수 없습니다: " + e.getMessage());
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 바로 구매용 CartDto 생성 (실제 장바구니에 저장하지 않고 주문 페이지 표시용)
     */
    private List<CartDto> createDirectPurchaseItems(int productCode, Integer optionCode, int quantity) {
        try {
            // ProductService를 통해 상품 상세 정보 조회
            ProductDto product = productService.getProductByCode(productCode);

            CartDto cartItem = new CartDto();
            cartItem.setProductCode(productCode);
            cartItem.setOptionCode(optionCode);
            cartItem.setQuantity(quantity);
            cartItem.setSelected(true);

            // 상품 정보 설정
            cartItem.setProductDto(product);

            // 가격 정보 설정
            PriceDto price = productService.getProductPrice(productCode);
            cartItem.setPriceDto(price);

            // 옵션 정보 설정 (옵션이 있는 경우)
            if (optionCode != null) {
                ProductOptionDto option = productService.getProductOption(optionCode);
                cartItem.setProductOptionDto(option);
            }

            // 이미지 정보 설정
            ImageDto image = productService.getProductTitleImage(productCode);
            cartItem.setImageDto(image);

            return List.of(cartItem);

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}