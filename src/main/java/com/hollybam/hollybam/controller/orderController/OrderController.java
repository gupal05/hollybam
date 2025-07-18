// 옵션 1: OrderController에 결제 API들을 모두 추가하는 방법
package com.hollybam.hollybam.controller.orderController;

import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.*;
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
    @Autowired
    private IF_OrderService orderService;
    @Autowired
    private IF_PointService pointService;

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
                }
            }

            if (cartCodeList.isEmpty()) {
                mav.setViewName("redirect:/cart");
                return mav;
            }

            Integer memberCode = member != null ? member.getMemberCode() : null;
            Integer guestCode = guest != null ? guest.getGuestCode() : null;

            PaymentRequestDto paymentInfo = paymentService.calculateOrderFromCart(
                    cartCodeList, memberCode, guestCode);

            if (paymentInfo == null) {
                log.error("PaymentInfo is null");
                mav.setViewName("redirect:/cart");
                return mav;
            }

            List<CartDto> cartItems = getCartItemsWithDetails(cartCodeList);

            if (cartItems == null || cartItems.isEmpty()) {
                log.error("CartItems is null or empty");
                mav.setViewName("redirect:/cart");
                return mav;
            }

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

            // ★ 핵심 수정: 둘 다 모델에 추가
            mav.addObject("member", member);
            mav.addObject("guest", guest);
            mav.addObject("paymentInfo", paymentInfo);
            mav.addObject("cartItems", cartItems);
            mav.addObject("cartCodes", cartCodeList);
            mav.addObject("couponList", couponList);
            mav.setViewName("order");

        } catch (Exception e) {
            log.error("주문 페이지 로드 중 예외 발생", e);
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

    /**
     * 할인코드 검증 API (🆕 중복 사용 방지 로직 포함)
     */
    @PostMapping("/discount/validate")
    public ResponseEntity<Map<String, Object>> validateDiscountCode(
            @RequestBody Map<String, Object> request, HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String discountId = (String) request.get("discountId");
            Long orderAmount = Long.valueOf(request.get("orderAmount").toString());

            if(session.getAttribute("member") != null){
                // 🆕 세션에서 회원 정보 가져오기
                MemberDto member = (MemberDto) session.getAttribute("member");
                Integer memCode = member != null ? member.getMemberCode() : null;

                log.info("할인코드 검증 요청: discountId={}, orderAmount={}, memCode={}",
                        discountId, orderAmount, memCode);

                // 🆕 회원 코드를 포함한 할인코드 검증 (중복 사용 체크 포함)
                Map<String, Object> validationResult = discountService.validateDiscountCode(discountId, orderAmount, memCode);

                response.put("success", true);
                response.put("data", validationResult.get("discountInfo"));
                response.put("discountAmount", validationResult.get("discountAmount"));
                response.put("message", "할인코드가 성공적으로 적용되었습니다.");

                log.info("할인코드 검증 성공: discountId={}, memCode={}, discountAmount={}",
                        discountId, memCode, validationResult.get("discountAmount"));
            } else if(session.getAttribute("guest") != null){
                // 🆕 세션에서 회원 정보 가져오기
                GuestDto guest = (GuestDto) session.getAttribute("guest");
                Integer guestCode = guest != null ? guest.getGuestCode() : null;

                log.info("할인코드 검증 요청: discountId={}, orderAmount={}, guestCode={}",
                        discountId, orderAmount, guestCode);

                // 🆕 회원 코드를 포함한 할인코드 검증 (중복 사용 체크 포함)
                Map<String, Object> validationResult = discountService.validateDiscountCode(discountId, orderAmount, guestCode);

                response.put("success", true);
                response.put("data", validationResult.get("discountInfo"));
                response.put("discountAmount", validationResult.get("discountAmount"));
                response.put("message", "할인코드가 성공적으로 적용되었습니다.");

                log.info("할인코드 검증 성공: discountId={}, memCode={}, discountAmount={}",
                        discountId, guestCode, validationResult.get("discountAmount"));
            }
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
                if (member != null) {
                    couponList = couponService.getUsePossibleCoupon(member.getMemberCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ModelAndView에 데이터 추가 (기존 checkout과 동일한 구조)
            if(member != null) {
                mav.addObject("member", member);
            } else {
                mav.addObject("guest", guest);
            }
            mav.addObject("paymentInfo", paymentInfo);
            mav.addObject("cartItems", cartItems);
            mav.addObject("productCode",  productCode);
            mav.addObject("optionCode",  optionCode);
            mav.addObject("quantity",  quantity);
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

    /**
     * 주문 생성 (장바구니에서)
     */
    @PostMapping("/create-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestBody Map<String, Object> orderData,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            if (member != null) {
                orderData.put("memCode", member.getMemberCode());
                orderData.put("adultVerified", member.isAdultVerified());
            } else {
                orderData.put("guestCode", guest.getGuestCode());
                orderData.put("adultVerified", guest.isAdultVerified());
            }

            Boolean adultVerified = (Boolean) orderData.get("adultVerified");
            if (!adultVerified) {
                result.put("success", false);
                result.put("message", "성인 인증이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            OrderDto order = orderService.createOrderFromCart(orderData);

            result.put("success", true);
            result.put("message", "주문이 완료되었습니다.");
            result.put("orderId", order.getOrderId());
            result.put("orderCode", order.getOrderCode());

        } catch (Exception e) {
            log.error("주문 생성 실패", e);
            result.put("success", false);
            result.put("message", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 바로 구매 주문 생성
     */
    @PostMapping("/create-direct-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createDirectOrder(
            @RequestBody Map<String, Object> orderData,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            if (member != null) {
                orderData.put("memCode", member.getMemberCode());
                orderData.put("adultVerified", member.isAdultVerified());
            } else {
                orderData.put("guestCode", guest.getGuestCode());
                orderData.put("adultVerified", guest.isAdultVerified());
            }

            Boolean adultVerified = (Boolean) orderData.get("adultVerified");
            if (!adultVerified) {
                result.put("success", false);
                result.put("message", "성인 인증이 필요합니다.");
                return ResponseEntity.ok(result);
            }

            OrderDto order = orderService.createDirectOrder(orderData);

            result.put("success", true);
            result.put("message", "주문이 완료되었습니다.");
            result.put("orderId", order.getOrderId());
            result.put("orderCode", order.getOrderCode());

        } catch (Exception e) {
            log.error("바로 구매 주문 생성 실패", e);
            result.put("success", false);
            result.put("message", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 주문 완료 페이지
     */
    @GetMapping("/order-complete/{orderId}")
    public ModelAndView orderComplete(@PathVariable String orderId) {
        ModelAndView mav = new ModelAndView();
        try {
            OrderDto order = orderService.getOrderDetail(orderId);
            List<Map<String, Object>> orderDetails = orderService.getOrderDetails(order.getOrderCode());
            mav.addObject("orderDetails", orderDetails);
            System.out.println(orderDetails);
            mav.addObject("order", order);
            mav.setViewName("paymentResult");

        } catch (Exception e) {
            log.error("주문 완료 페이지 조회 실패: {}", orderId, e);
            mav.addObject("error", "주문 정보를 불러올 수 없습니다.");
            mav.setViewName("error/error");
        }

        return mav;
    }

    /**
     * 주문 취소
     */
    @PostMapping("/cancel/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable String orderId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();

        try {
            OrderDto order = orderService.getOrderDetail(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "주문을 찾을 수 없습니다.");
                return ResponseEntity.ok(result);
            }

            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            boolean hasPermission = false;
            if (member != null && order.getMemCode() != null &&
                    member.getMemberCode() == order.getMemCode()) {
                hasPermission = true;
            } else if (guest != null && order.getGuestCode() != null &&
                    guest.getGuestCode() == order.getGuestCode()) {
                hasPermission = true;
            }

            if (!hasPermission) {
                result.put("success", false);
                result.put("message", "주문 취소 권한이 없습니다.");
                return ResponseEntity.ok(result);
            }

            if (!order.getOrderStatus().equals("PAID") && !order.getOrderStatus().equals("PREPARING")) {
                result.put("success", false);
                result.put("message", "현재 주문 상태에서는 취소가 불가능합니다.");
                return ResponseEntity.ok(result);
            }

            boolean cancelled = orderService.cancelOrder(order.getOrderCode(), orderId);

            if (cancelled) {
                result.put("success", true);
                result.put("message", "주문이 취소되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "주문 취소 처리에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("주문 취소 실패: {}", orderId, e);
            result.put("success", false);
            result.put("message", "주문 취소 중 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 위시리스트에서 바로구매를 위한 주문 페이지
     */
    @GetMapping("/direct")
    public String directOrderPage(HttpSession session, Model model) {
        try {
            // 세션에서 바로구매 데이터 확인
            Map<String, Object> directPurchaseData = (Map<String, Object>) session.getAttribute("directPurchaseData");

            if (directPurchaseData == null) {
                return "redirect:/main"; // 잘못된 접근
            }

            // 사용자 정보 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                return "redirect:/intro"; // 로그인 필요
            }

            // 상품 정보 조회
            Integer productCode = (Integer) directPurchaseData.get("productCode");
            Integer quantity = (Integer) directPurchaseData.get("quantity");
            Integer optionCode = (Integer) directPurchaseData.get("optionCode");

            // ProductService를 통해 상품 정보 조회
            ProductDto product = productService.getProductByCode(productCode);
            if (product == null) {
                session.removeAttribute("directPurchaseData");
                return "redirect:/main";
            }

            // 가격 정보 계산
            PriceDto priceInfo = productService.getProductPrice(productCode);
            int unitPrice = priceInfo.getPriceSelling();
            int totalPrice = unitPrice * quantity;

            // 배송비 계산 (예: 50,000원 이상 무료배송)
            int deliveryFee = totalPrice >= 50000 ? 0 : 3000;
            int finalAmount = totalPrice + deliveryFee;

            // 모델에 데이터 추가
            model.addAttribute("product", product);
            model.addAttribute("quantity", quantity);
            model.addAttribute("unitPrice", unitPrice);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("deliveryFee", deliveryFee);
            model.addAttribute("finalAmount", finalAmount);
            model.addAttribute("orderType", "direct");

            // 회원 정보 추가
            if (member != null) {
                model.addAttribute("member", member);
            } else {
                model.addAttribute("guest", guest);
            }

            return "order/orderForm"; // 주문 폼 페이지

        } catch (Exception e) {
            log.error("바로구매 주문 페이지 로드 실패", e);
            return "redirect:/main";
        }
    }

    /**
     * 위시리스트에서 선택 구매를 위한 임시 주문 페이지
     */
    @GetMapping("/temp")
    public String tempOrderPage(HttpSession session, Model model) {
        try {
            // 세션에서 임시 주문 데이터 확인
            List<Map<String, Object>> tempOrderData = (List<Map<String, Object>>) session.getAttribute("tempOrderData");

            if (tempOrderData == null || tempOrderData.isEmpty()) {
                return "redirect:/main"; // 잘못된 접근
            }

            // 사용자 정보 확인
            MemberDto member = (MemberDto) session.getAttribute("member");
            GuestDto guest = (GuestDto) session.getAttribute("guest");

            if (member == null && guest == null) {
                return "redirect:/intro"; // 로그인 필요
            }

            // 상품 정보들 조회
            List<Map<String, Object>> orderItems = new ArrayList<>();
            int totalPrice = 0;

            for (Map<String, Object> item : tempOrderData) {
                Integer productCode = (Integer) item.get("productCode");
                Integer quantity = (Integer) item.get("quantity");

                // 상품 정보 조회
                ProductDto product = productService.getProductByCode(productCode);
                if (product == null) {
                    throw new Exception("상품을 찾을 수 없습니다. productCode: " + productCode);
                }
                if (product == null) continue;

                // 가격 정보 조회
                PriceDto priceInfo = productService.getProductPrice(productCode);
                int unitPrice = priceInfo.getPriceSelling();
                int itemTotal = unitPrice * quantity;
                totalPrice += itemTotal;

                // 주문 아이템 정보 생성
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("product", product);
                orderItem.put("quantity", quantity);
                orderItem.put("unitPrice", unitPrice);
                orderItem.put("itemTotal", itemTotal);

                orderItems.add(orderItem);
            }

            // 배송비 계산
            int deliveryFee = totalPrice >= 50000 ? 0 : 3000;
            int finalAmount = totalPrice + deliveryFee;

            // 모델에 데이터 추가
            model.addAttribute("orderItems", orderItems);
            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("deliveryFee", deliveryFee);
            model.addAttribute("finalAmount", finalAmount);
            model.addAttribute("orderType", "temp");

            // 회원 정보 추가
            if (member != null) {
                model.addAttribute("member", member);
            } else {
                model.addAttribute("guest", guest);
            }

            return "order/orderForm"; // 주문 폼 페이지

        } catch (Exception e) {
            log.error("임시 주문 페이지 로드 실패", e);
            return "redirect:/main";
        }
    }

    /**
     * 바로구매 주문 처리
     */
    @PostMapping("/direct/process")
    @ResponseBody
    public Map<String, Object> processDirectOrder(@RequestBody Map<String, Object> orderData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션에서 바로구매 데이터 확인
            Map<String, Object> directPurchaseData = (Map<String, Object>) session.getAttribute("directPurchaseData");

            if (directPurchaseData == null) {
                response.put("success", false);
                response.put("message", "주문 정보를 찾을 수 없습니다.");
                return response;
            }

            // 주문 데이터에 상품 정보 추가
            orderData.put("productCode", directPurchaseData.get("productCode"));
            orderData.put("quantity", directPurchaseData.get("quantity"));
            orderData.put("optionCode", directPurchaseData.get("optionCode"));

            // 주문 생성
            OrderDto order = orderService.createDirectOrder(orderData);

            if (order != null) {
                // 세션 데이터 정리
                session.removeAttribute("directPurchaseData");

                response.put("success", true);
                response.put("orderId", order.getOrderId());
                response.put("message", "주문이 성공적으로 생성되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "주문 생성에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("바로구매 주문 처리 실패", e);
            response.put("success", false);
            response.put("message", "주문 처리 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 임시 주문 처리
     */
    @PostMapping("/temp/process")
    @ResponseBody
    public Map<String, Object> processTempOrder(@RequestBody Map<String, Object> orderData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 세션에서 임시 주문 데이터 확인
            List<Map<String, Object>> tempOrderData = (List<Map<String, Object>>) session.getAttribute("tempOrderData");

            if (tempOrderData == null || tempOrderData.isEmpty()) {
                response.put("success", false);
                response.put("message", "주문 정보를 찾을 수 없습니다.");
                return response;
            }

            // 주문 데이터에 상품 정보들 추가
            orderData.put("orderItems", tempOrderData);

            // 주문 생성 (장바구니 주문과 유사한 방식)
            OrderDto order = orderService.createTempOrder(orderData);

            if (order != null) {
                // 세션 데이터 정리
                session.removeAttribute("tempOrderData");

                response.put("success", true);
                response.put("orderId", order.getOrderId());
                response.put("message", "주문이 성공적으로 생성되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "주문 생성에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("임시 주문 처리 실패", e);
            response.put("success", false);
            response.put("message", "주문 처리 중 오류가 발생했습니다.");
        }

        return response;
    }

    /**
     * 회원 적립금 조회 API
     */
    @GetMapping("/api/points")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMemberPoints(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }

            int currentPoints = pointService.getCurrentPoints(member.getMemberCode());

            response.put("success", true);
            response.put("currentPoints", currentPoints);

        } catch (Exception e) {
            log.error("적립금 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "적립금 조회 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 적립금 사용 가능 여부 검증 API
     */
    @PostMapping("/api/points/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validatePointUsage(
            @RequestBody Map<String, Object> requestData,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            MemberDto member = (MemberDto) session.getAttribute("member");
            if (member == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.ok(response);
            }

            int usePoints = (Integer) requestData.get("usePoints");
            int currentPoints = pointService.getCurrentPoints(member.getMemberCode());

            if (currentPoints >= usePoints) {
                response.put("success", true);
                response.put("available", true);
                response.put("remainPoints", currentPoints - usePoints);
            } else {
                response.put("success", true);
                response.put("available", false);
                response.put("message", "보유 적립금이 부족합니다.");
                response.put("currentPoints", currentPoints);
                response.put("shortfall", usePoints - currentPoints);
            }

        } catch (Exception e) {
            log.error("적립금 검증 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "적립금 검증 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }
}