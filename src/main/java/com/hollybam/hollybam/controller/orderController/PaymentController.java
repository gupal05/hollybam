//package com.hollybam.hollybam.controller.orderController;
//
//import com.hollybam.hollybam.dto.MemberDto;
//import com.hollybam.hollybam.dto.OrderDto;
//import com.hollybam.hollybam.dto.PaymentRequestDto;
//import com.hollybam.hollybam.dto.PaymentResponseDto;
//import com.hollybam.hollybam.services.IF_PaymentService;
//import jakarta.servlet.http.HttpSession;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/payment")
//public class PaymentController {
//
//    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
//
//    @Autowired
//    private IF_PaymentService paymentService;
//
//    /**
//     * 주문 결제 페이지 표시 (order.html)
//     */
//    @GetMapping("/checkout")
//    public String checkoutPage(@RequestParam List<Integer> cartCodes,
//                               HttpSession session, Model model) {
//        try {
//            // 세션에서 사용자 정보 가져오기
//            MemberDto member = (MemberDto) session.getAttribute("loginMember");
//            String guestUuid = (String) session.getAttribute("guestUuid");
//
//            // 성인인증 확인
//            boolean isAdultVerified = paymentService.verifyAdultCertification(
//                    member != null ? member.getMemberCode() : null, guestUuid);
//
//            if (!isAdultVerified) {
//                model.addAttribute("error", "성인인증이 필요합니다.");
//                return "redirect:/adult-verification";
//            }
//
//            // 주문 정보 계산
//            PaymentRequestDto paymentInfo = paymentService.calculateOrderFromCart(
//                    cartCodes,
//                    member != null ? member.getMemberCode() : null,
//                    guestUuid);
//
//            model.addAttribute("member", member);
//            model.addAttribute("paymentInfo", paymentInfo);
//            model.addAttribute("cartCodes", cartCodes);
//
//            // order.html 반환
//            return "order";
//
//        } catch (Exception e) {
//            logger.error("결제 페이지 로드 실패", e);
//            model.addAttribute("error", "결제 페이지를 불러올 수 없습니다: " + e.getMessage());
//            return "error/error";
//        }
//    }
//
//    /**
//     * 결제 준비 API
//     */
//    @PostMapping("/prepare")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> preparePayment(
//            @RequestBody PaymentRequestDto paymentRequestDto,
//            HttpSession session) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            // 세션에서 사용자 정보 설정
//            MemberDto member = (MemberDto) session.getAttribute("loginMember");
//            String guestUuid = (String) session.getAttribute("guestUuid");
//
//            if (member != null) {
//                paymentRequestDto.setMemCode(member.getMemberCode());
//                paymentRequestDto.setBuyerBirth(member.getMemberBirth());
//            } else if (guestUuid != null) {
//                paymentRequestDto.setGuestUuid(guestUuid);
//            } else {
//                response.put("success", false);
//                response.put("message", "로그인이 필요합니다.");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            // 성인인증 확인
//            paymentRequestDto.setAdultVerified(
//                    paymentService.verifyAdultCertification(paymentRequestDto.getMemCode(), guestUuid));
//
//            // 결제 준비
//            String orderId = paymentService.preparePayment(paymentRequestDto);
//
//            response.put("success", true);
//            response.put("orderId", orderId);
//            response.put("message", "결제 준비가 완료되었습니다.");
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("결제 준비 실패", e);
//            response.put("success", false);
//            response.put("message", e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//    /**
//     * 결제 완료 처리 API
//     */
//    @PostMapping("/complete")
//    @ResponseBody
//    public ResponseEntity<PaymentResponseDto> completePayment(
//            @RequestParam String imp_uid,
//            @RequestParam String merchant_uid) {
//
//        try {
//            PaymentResponseDto response = paymentService.completePayment(imp_uid, merchant_uid);
//
//            if (response.isSuccess()) {
//                logger.info("결제 완료: orderId={}, impUid={}, amount={}",
//                        response.getOrderId(), response.getImpUid(), response.getAmount());
//            } else {
//                logger.warn("결제 실패: orderId={}, error={}", merchant_uid, response.getErrorMsg());
//            }
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("결제 완료 처리 실패", e);
//
//            PaymentResponseDto response = new PaymentResponseDto();
//            response.setSuccess(false);
//            response.setMessage("결제 처리 중 오류가 발생했습니다.");
//            response.setErrorMsg(e.getMessage());
//
//            return ResponseEntity.ok(response);
//        }
//    }
//
//    /**
//     * 결제 실패 처리 API
//     */
//    @PostMapping("/fail")
//    @ResponseBody
//    public ResponseEntity<Map<String, Object>> failPayment(
//            @RequestParam String merchant_uid,
//            @RequestParam String error_msg) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            paymentService.failPayment(merchant_uid, error_msg);
//            logger.warn("결제 실패 처리: orderId={}, error={}", merchant_uid, error_msg);
//
//            response.put("success", true);
//            response.put("message", "결제 실패 처리가 완료되었습니다.");
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            logger.error("결제 실패 처리 오류", e);
//            response.put("success", false);
//            response.put("message", "결제 실패 처리 중 오류가 발생했습니다.");
//            return ResponseEntity.ok(response);
//        }
//    }
//
//    /**
//     * 결제 완료 페이지 (paymentResult.html)
//     */
//    @GetMapping("/success")
//    public String paymentSuccess(@RequestParam String orderId, Model model) {
//        try {
//            OrderDto order = paymentService.getOrder(orderId);
//
//            if (order == null) {
//                model.addAttribute("error", "주문 정보를 찾을 수 없습니다.");
//                return "error/error";
//            }
//
//            model.addAttribute("order", order);
//
//            // paymentResult.html 반환
//            return "paymentResult";
//
//        } catch (Exception e) {
//            logger.error("결제 완료 페이지 로드 실패", e);
//            model.addAttribute("error", "결제 완료 페이지를 불러올 수 없습니다.");
//            return "error/error";
//        }
//    }
//
//    /**
//     * 결제 실패 페이지 (paymentFail.html)
//     */
//    @GetMapping("/fail")
//    public String paymentFail(@RequestParam(required = false) String orderId,
//                              @RequestParam(required = false) String error_msg,
//                              Model model) {
//        model.addAttribute("orderId", orderId);
//        model.addAttribute("errorMsg", error_msg);
//
//        // paymentFail.html 반환
//        return "paymentFail";
//    }
//
//    /**
//     * 주문 내역 조회
//     */
//    @GetMapping("/orders")
//    public String orderHistory(HttpSession session, Model model) {
//        try {
//            MemberDto member = (MemberDto) session.getAttribute("loginMember");
//            String guestUuid = (String) session.getAttribute("guestUuid");
//
//            List<OrderDto> orders;
//            if (member != null) {
//                orders = paymentService.getOrdersByMember(member.getMemberCode());
//            } else if (guestUuid != null) {
//                orders = paymentService.getOrdersByGuest(guestUuid);
//            } else {
//                model.addAttribute("error", "로그인이 필요합니다.");
//                return "redirect:/login";
//            }
//
//            model.addAttribute("orders", orders);
//            model.addAttribute("member", member);
//
//            return "payment/order-history";
//
//        } catch (Exception e) {
//            logger.error("주문 내역 조회 실패", e);
//            model.addAttribute("error", "주문 내역을 불러올 수 없습니다.");
//            return "error/error";
//        }
//    }
//
//    /**
//     * 주문 상세 조회
//     */
//    @GetMapping("/order/{orderId}")
//    public String orderDetail(@PathVariable String orderId,
//                              HttpSession session, Model model) {
//        try {
//            OrderDto order = paymentService.getOrder(orderId);
//
//            if (order == null) {
//                model.addAttribute("error", "주문 정보를 찾을 수 없습니다.");
//                return "error/error";
//            }
//
//            // 권한 확인
//            MemberDto member = (MemberDto) session.getAttribute("loginMember");
//            String guestUuid = (String) session.getAttribute("guestUuid");
//
//            boolean hasPermission = false;
//            if (member != null && order.getMemCode() != null &&
//                    member.getMemberCode() == order.getMemCode()) {
//                hasPermission = true;
//            } else if (guestUuid != null && order.getGuestCode() != null) {
//                // 비회원의 경우 추가 인증 필요 (휴대폰 번호 등)
//                hasPermission = true; // 임시로 허용, 실제로는 추가 인증 구현
//            }
//
//            if (!hasPermission) {
//                model.addAttribute("error", "주문 정보에 접근할 권한이 없습니다.");
//                return "error/error";
//            }
//
//            model.addAttribute("order", order);
//            return "payment/order-detail";
//
//        } catch (Exception e) {
//            logger.error("주문 상세 조회 실패", e);
//            model.addAttribute("error", "주문 상세 정보를 불러올 수 없습니다.");
//            return "error/error";
//        }
//    }
//
//    /**
//     * 모바일 결제 콜백 (웹훅)
//     */
//    @PostMapping("/webhook")
//    @ResponseBody
//    public ResponseEntity<String> paymentWebhook(@RequestBody String payload) {
//        try {
//            logger.info("결제 웹훅 수신: {}", payload);
//            // 웹훅 처리 로직 (필요시 구현)
//            return ResponseEntity.ok("OK");
//        } catch (Exception e) {
//            logger.error("웹훅 처리 실패", e);
//            return ResponseEntity.ok("FAIL");
//        }
//    }
//}
//
