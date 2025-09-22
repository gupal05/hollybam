package com.hollybam.hollybam.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hollybam.hollybam.dao.IF_OrderDao;
import com.hollybam.hollybam.dto.*;
import com.hollybam.hollybam.services.CouponService;
import com.hollybam.hollybam.services.OrderServiceImpl;
import com.hollybam.hollybam.services.PayService;
import com.hollybam.hollybam.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/pay")
public class PayController {
    @Autowired
    private OrderServiceImpl orderService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private IF_OrderDao orderDao;
    @Autowired
    private ObjectMapper mapper;
    private final PayService service;
    private final PaysterProperties prop;
    private final RestTemplate rest = new RestTemplate();
    @Autowired
    private PaymentService paymentService;

    public PayController(PayService service, PaysterProperties prop) {
        this.service = service;
        this.prop    = prop;
    }

    /** 1) 결제 요청 폼 렌더링 */
    @GetMapping
    public String payForm(HttpServletRequest req, Model m) {
        String ediDate  = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String goodsAmt = "1004";
        String ordNo    = String.valueOf(System.currentTimeMillis());
        String encData  = service.makeEncData(ediDate, goodsAmt);

        m.addAttribute("payMethod",    "card");
        m.addAttribute("mid",          prop.getMid());
        m.addAttribute("trxCd",        "0");
        m.addAttribute("goodsNm",      "테스트상품");
        m.addAttribute("ordNo",        ordNo);
        m.addAttribute("ordNm",        "홍길동");
        m.addAttribute("ordTel",       "01012345678");
        m.addAttribute("ordEmail",     "test@example.com");
        m.addAttribute("goodsAmt",     goodsAmt);
        m.addAttribute("ediDate",      ediDate);
        m.addAttribute("encData",      encData);
        m.addAttribute("currencyType", "KWR");
        m.addAttribute("channel",      "0001");
        m.addAttribute("charset",      "UTF-8");
        m.addAttribute("appMode",      "1");
        m.addAttribute("userIp",       req.getRemoteAddr());
        m.addAttribute("returnUrl",    prop.getReturnUrl());
        m.addAttribute("notiUrl",      prop.getNotiUrl());

        return "payForm";  // src/main/resources/templates/payForm.html
    }

    /** 2) PG → 내 서버 콜백 (인증 결과 수신 & 해시 검증 & 승인 요청) */
    @RequestMapping(value = "/result", method = { RequestMethod.GET, RequestMethod.POST })
    public String payResult(
            @RequestParam Map<String, String> p,
            Model m, HttpSession session
    ) throws Exception {

        // 1) PG 모듈이 준 모든 파라미터
        m.addAttribute("params", p);

        // 2) 인증 결과가 성공인 경우에만 승인 요청
        if ("0000".equals(p.get("resultCode"))) {
            String tid = p.get("tid");
            String ediDate = p.get("ediDate");
            String goodsAmt = p.get("goodsAmt");
            String ordNo = p.get("ordNo");

            // a) encData 재생성
            String encData = service.makeEncData(ediDate, goodsAmt);
            String signData = p.get("signData");

            // b) 승인 API 호출용 form
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("mid", prop.getMid());
            form.add("tid", tid);
            form.add("goodsAmt", goodsAmt);
            form.add("ediDate", ediDate);
            form.add("charSet", "UTF-8");
            form.add("encData", encData);
            form.add("signData", signData);

            // c) 🎯 핵심: PG사에 승인 요청
            String approveRes = rest.postForObject(prop.getUrlApprove(), form, String.class);
            m.addAttribute("approveRes", approveRes);
            System.out.println("PG 승인 응답: " + approveRes);

            // d) JSON 파싱
            JsonNode root = mapper.readTree(approveRes);
            String resultCd = root.path("resultCd").asText();
            String resultMsg = root.path("resultMsg").asText();

            if ("3001".equals(resultCd)) {
                // ✅ 실제 승인 성공 - 주문 생성
                try {
                    Map<String, Object> orderData = (Map<String, Object>) session.getAttribute("pendingOrderData");
                    MemberDto member = (MemberDto) session.getAttribute("pendingMember");
                    GuestDto guest = (GuestDto) session.getAttribute("pendingGuest");

                    if (orderData == null) {
                        throw new Exception("주문 정보를 찾을 수 없습니다.");
                    }

                    // 🎯 장바구니/바로구매 구분해서 주문 생성
                    OrderDto order;
                    @SuppressWarnings("unchecked")
                    List<Integer> cartCodes = (List<Integer>) orderData.get("cartCodes");

                    if (cartCodes != null && !cartCodes.isEmpty()) {
                        // 장바구니 결제
                        orderData.put("payResult", true);
                        order = orderService.createOrderFromCart(orderData);
                    } else if (orderData.containsKey("productCode")) {
                        // 바로구매
                        orderData.put("payResult", true);
                        order = orderService.createDirectOrder(orderData);
                    } else {
                        throw new Exception("주문 타입을 판단할 수 없습니다.");
                    }

                    String mc = null;
                    if(member != null){
                        mc = String.valueOf(member.getMemberCode());
                    } else if(guest != null){
                        mc = String.valueOf(guest.getGuestCode());
                    }
                    // PAID 상태로 설정
                    orderService.updatePaymentStatus(order.getOrderId()+"_"+mc, "PAID");

                    // 결제 로그 저장
                    PaymentLogDto paymentLogDto = new PaymentLogDto();
                    paymentLogDto.setTid(tid);
                    paymentLogDto.setPayMethod("card");
                    paymentLogDto.setAmount(Integer.parseInt(goodsAmt));
                    paymentLogDto.setResultCode(resultCd); // "3001"
                    paymentLogDto.setResultMsg(resultMsg);
                    paymentLogDto.setOrderCode(order.getOrderCode());

                    if (member != null) {
                        paymentLogDto.setMemberCode(member.getMemberCode());
                    } else if (guest != null) {
                        paymentLogDto.setGuestCode(guest.getGuestCode());
                    }

                    paymentService.insertPaymentLog(paymentLogDto);

                    // 세션 정리
                    session.removeAttribute("pendingOrderData");
                    session.removeAttribute("pendingMember");
                    session.removeAttribute("pendingGuest");
                    session.removeAttribute("tempOrderId");

                    int memberCode = 0;
                    if(session.getAttribute("member") != null){
                        MemberDto mem = (MemberDto) session.getAttribute("member");
                        memberCode = mem.getMemberCode();
                    } else if (session.getAttribute("guest") != null) {
                        GuestDto gu = (GuestDto) session.getAttribute("guest");
                        memberCode = gu.getGuestCode();
                    }
                    String id = order.getOrderId()+"_"+memberCode;
                    return "redirect:/order/order-complete/" + id;

                } catch (Exception e) {
                    log.error("결제 성공 후 주문 생성 실패", e);
                    m.addAttribute("errorMsg", "결제는 성공했지만 주문 처리 중 오류가 발생했습니다.");
                    return "paymentFail";
                }

            } else {
                // ❌ PG 승인 실패 - 결제 실패 처리
                try {
                    Map<String, Object> orderData = (Map<String, Object>) session.getAttribute("pendingOrderData");
                    MemberDto member = (MemberDto) session.getAttribute("pendingMember");
                    GuestDto guest = (GuestDto) session.getAttribute("pendingGuest");

                    if (orderData != null) {
                        OrderDto order;
                        @SuppressWarnings("unchecked")
                        List<Integer> cartCodes = (List<Integer>) orderData.get("cartCodes");

                        if (cartCodes != null && !cartCodes.isEmpty()) {
                            orderData.put("payResult", false);
                            order = orderService.createOrderFromCart(orderData);
                        } else if (orderData.containsKey("productCode")) {
                            orderData.put("payResult", false);
                            order = orderService.createDirectOrder(orderData);
                        } else {
                            throw new Exception("주문 타입을 판단할 수 없습니다.");
                        }

                        String mcd = null;
                        if(member != null){
                            mcd = String.valueOf(member.getMemberCode());
                        } else if(guest != null){
                            mcd = String.valueOf(guest.getGuestCode());
                        }
                        orderService.updatePaymentStatus(order.getOrderId()+"_"+mcd, "FAILED");

                        // 결제 실패 로그 저장
                        PaymentLogDto paymentLogDto = new PaymentLogDto();
                        paymentLogDto.setTid(tid);
                        paymentLogDto.setPayMethod("card");
                        paymentLogDto.setAmount(Integer.parseInt(goodsAmt));
                        paymentLogDto.setResultCode(resultCd);
                        paymentLogDto.setResultMsg(resultMsg);
                        paymentLogDto.setOrderCode(order.getOrderCode());

                        if (member != null) {
                            paymentLogDto.setMemberCode(member.getMemberCode());
                        } else if (guest != null) {
                            paymentLogDto.setGuestCode(guest.getGuestCode());
                        }

                        paymentService.insertPaymentLog(paymentLogDto);
                    }

                } catch (Exception e) {
                    log.error("결제 실패 처리 중 오류", e);
                }

                // 세션 정리
                session.removeAttribute("pendingOrderData");
                session.removeAttribute("pendingMember");
                session.removeAttribute("pendingGuest");
                session.removeAttribute("tempOrderId");

                m.addAttribute("errorMsg", "결제 승인 실패: " + resultMsg);
                return "paymentFail";
            }

        } else {
            // ❌ 인증 실패
            session.removeAttribute("pendingOrderData");
            session.removeAttribute("pendingMember");
            session.removeAttribute("pendingGuest");
            session.removeAttribute("tempOrderId");

            m.addAttribute("errorMsg", "결제 인증 실패: " + p.get("resultMsg"));
            return "paymentFail";
        }
    }
    /** 3) 백엔드 알림용 콜백 */
    @PostMapping("/notify")
    @ResponseBody
    public String payNotify(@RequestParam Map<String,String> p) {
        // 예: System.out.println(p);
        return "OK";
    }
}

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//
//import java.security.MessageDigest;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Map;
//import java.util.HashMap;
//
///**
// * PDF에 기재된 실제 Payster 연동 방법
// */
//@Controller
//@RequestMapping("/payment")
//public class PayController {
//
//    // PDF에서 제공된 테스트 정보 (실제로는 properties에서 가져와야 함)
//    @Value("${payster.mid}")
//    private String MID;
//    @Value("${payster.merchant_key}")
//    private String MERCHANT_KEY;
//
//    // PDF에서 명시된 API URLs
//    @Value("${payster.payment_init_url}")
//    private String PAYMENT_INIT_URL;
//    @Value("${payster.payment_approve_url}")
//    private String PAYMENT_APPROVE_URL;
//    @Value("${payster.payment_cancel_url}")
//    private String PAYMENT_CANCEL_URL;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    /**
//     * 결제 페이지 표시
//     */
//    @GetMapping("/test")
//    public String paymentTest(Model model) {
//        model.addAttribute("mid", MID);
//        model.addAttribute("merchant_key", MERCHANT_KEY);
//        model.addAttribute("paymentInitUrl", PAYMENT_INIT_URL);
//        return "payForm"; // 위에서 만든 HTML 페이지
//    }
//
//    /**
//     * PDF 3.1.2에서 명시된 인증 결과 수신
//     * Payster에서 returnUrl로 POST 요청을 보냄
//     */
//    @PostMapping("/result")
//    public String receiveAuthResult(@RequestParam Map<String, String> params, Model model) {
//        System.out.println("=== Payster 인증 결과 수신 ===");
//        params.forEach((key, value) -> System.out.println(key + ": " + value));
//
//        String resultCode = params.get("resultCode");
//        String resultMsg = params.get("resultMsg");
//        String tid = params.get("tid");
//        String ordNo = params.get("ordNo");
//        String goodsAmt = params.get("goodsAmt");
//        String signData = params.get("signData");
//
//        if ("0000".equals(resultCode)) {
//            // 인증 성공 - PDF 3.1.3에 따라 승인 요청
//            try {
//                Map<String, String> approvalResult = requestApproval(params);
//
//                if ("0000".equals(approvalResult.get("resultCd"))) {
//                    // 승인 성공
//                    model.addAttribute("success", true);
//                    model.addAttribute("message", "결제가 완료되었습니다.");
//                    model.addAttribute("ordNo", ordNo);
//                    model.addAttribute("amt", approvalResult.get("amt"));
//                    model.addAttribute("appNo", approvalResult.get("appNo"));
//                } else {
//                    // 승인 실패
//                    model.addAttribute("success", false);
//                    model.addAttribute("message", "결제 승인 실패: " + approvalResult.get("resultMsg"));
//                }
//            } catch (Exception e) {
//                model.addAttribute("success", false);
//                model.addAttribute("message", "승인 요청 오류: " + e.getMessage());
//            }
//        } else {
//            // 인증 실패
//            model.addAttribute("success", false);
//            model.addAttribute("message", "결제 인증 실패: " + resultMsg);
//        }
//
//        return "payment-result";
//    }
//
//    /**
//     * PDF 3.1.3에서 명시된 승인 요청
//     */
//    private Map<String, String> requestApproval(Map<String, String> authResult) throws Exception {
//        System.out.println("=== Payster 승인 요청 시작 ===");
//
//        String tid = authResult.get("tid");
//        String ediDate = authResult.get("ediDate");
//        String mid = authResult.get("mid");
//        String goodsAmt = authResult.get("goodsAmt");
//        String signData = authResult.get("signData");
//
//        // PDF에서 명시된 encData 생성 (승인 시에도 동일한 방식)
//        String encData = generateSHA256Hash(mid + ediDate + goodsAmt + MERCHANT_KEY);
//
//        // PDF 3.1.3에서 명시된 승인 요청 필드
//        MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
//        requestData.add("tid", tid);
//        requestData.add("ediDate", ediDate);
//        requestData.add("mid", mid);
//        requestData.add("goodsAmt", goodsAmt);
//        requestData.add("charSet", "UTF-8");
//        requestData.add("encData", encData);
//        requestData.add("signData", signData);
//
//        // HTTP 요청 설정
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.set("Charset", "UTF-8");
//
//        HttpEntity<MultiValueMap<String, String>> requestEntity =
//                new HttpEntity<>(requestData, headers);
//
//        System.out.println("승인 요청 URL: " + PAYMENT_APPROVE_URL);
//        System.out.println("승인 요청 데이터: " + requestData);
//
//        // Payster 승인 API 호출
//        ResponseEntity<String> response = restTemplate.postForEntity(
//                PAYMENT_APPROVE_URL, requestEntity, String.class);
//
//        System.out.println("승인 응답: " + response.getBody());
//
//        // 응답 파싱 (key=value&key=value 형태)
//        return parseResponse(response.getBody());
//    }
//
//    /**
//     * PDF 3.2에서 명시된 결제 취소
//     */
//    @PostMapping("/cancel")
//    @ResponseBody
//    public Map<String, Object> cancelPayment(@RequestParam String ordNo,
//                                             @RequestParam String canAmt,
//                                             @RequestParam String canMsg) {
//        Map<String, Object> result = new HashMap<>();
//
//        try {
//            // 실제로는 DB에서 주문 정보를 가져와야 함
//            String tid = "실제_거래번호"; // DB에서 조회
//            String ediDate = getCurrentEdiDate();
//
//            // PDF 3.2.1에서 명시된 취소 요청 필드
//            MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
//            requestData.add("tid", tid);
//            requestData.add("ordNo", ordNo);
//            requestData.add("canAmt", canAmt);
//            requestData.add("canMsg", canMsg);
//            requestData.add("canIp", "127.0.0.1");
//            requestData.add("partCanFlg", "0"); // 전체취소
//            requestData.add("ediDate", ediDate);
//
//            // encData 생성
//            String encData = generateSHA256Hash(MID + ediDate + canAmt + MERCHANT_KEY);
//            requestData.add("encData", encData);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//            HttpEntity<MultiValueMap<String, String>> requestEntity =
//                    new HttpEntity<>(requestData, headers);
//
//            ResponseEntity<String> response = restTemplate.postForEntity(
//                    PAYMENT_CANCEL_URL, requestEntity, String.class);
//
//            Map<String, String> cancelResult = parseResponse(response.getBody());
//
//            result.put("success", "0000".equals(cancelResult.get("resultCd")));
//            result.put("message", cancelResult.get("resultMsg"));
//
//        } catch (Exception e) {
//            result.put("success", false);
//            result.put("message", "취소 요청 오류: " + e.getMessage());
//        }
//
//        return result;
//    }
//
//    /**
//     * PDF 7.1에서 명시된 SHA256 암호화
//     * encData = Hex(SHA256(mid + ediDate + goodsAmt + merchantKey))
//     */
//    private String generateSHA256Hash(String data) throws Exception {
//        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        byte[] hash = digest.digest(data.getBytes("UTF-8"));
//
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : hash) {
//            String hex = Integer.toHexString(0xff & b);
//            if (hex.length() == 1) {
//                hexString.append('0');
//            }
//            hexString.append(hex);
//        }
//        return hexString.toString().toLowerCase();
//    }
//
//    /**
//     * PDF에서 요구하는 ediDate 형식 (yyyymmddhhmmss)
//     */
//    private String getCurrentEdiDate() {
//        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//    }
//
//    /**
//     * Payster 응답 파싱 (key=value&key=value 형태)
//     */
//    private Map<String, String> parseResponse(String responseBody) {
//        Map<String, String> result = new HashMap<>();
//
//        if (responseBody != null && !responseBody.trim().isEmpty()) {
//            String[] pairs = responseBody.split("&");
//            for (String pair : pairs) {
//                String[] keyValue = pair.split("=", 2);
//                if (keyValue.length == 2) {
//                    try {
//                        String key = keyValue[0];
//                        String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
//                        result.put(key, value);
//                    } catch (Exception e) {
//                        System.err.println("응답 파싱 오류: " + pair);
//                    }
//                }
//            }
//        }
//
//        return result;
//    }
//}