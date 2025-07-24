package com.hollybam.hollybam.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * PDF에 기재된 실제 Payster 연동 방법
 */
@Controller
@RequestMapping("/payment")
public class PayController {

    // PDF에서 제공된 테스트 정보 (실제로는 properties에서 가져와야 함)
    @Value("${payster.mid}")
    private String MID;
    @Value("${payster.merchant_key}")
    private String MERCHANT_KEY;

    // PDF에서 명시된 API URLs
    @Value("${payster.payment_init_url}")
    private String PAYMENT_INIT_URL;
    @Value("${payster.payment_approve_url}")
    private String PAYMENT_APPROVE_URL;
    @Value("${payster.payment_cancel_url}")
    private String PAYMENT_CANCEL_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 결제 페이지 표시
     */
    @GetMapping("/test")
    public String paymentTest(Model model) {
        model.addAttribute("mid", MID);
        model.addAttribute("merchant_key", MERCHANT_KEY);
        model.addAttribute("paymentInitUrl", PAYMENT_INIT_URL);
        return "payForm"; // 위에서 만든 HTML 페이지
    }

    /**
     * PDF 3.1.2에서 명시된 인증 결과 수신
     * Payster에서 returnUrl로 POST 요청을 보냄
     */
    @PostMapping("/result")
    public String receiveAuthResult(@RequestParam Map<String, String> params, Model model) {
        System.out.println("=== Payster 인증 결과 수신 ===");
        params.forEach((key, value) -> System.out.println(key + ": " + value));

        String resultCode = params.get("resultCode");
        String resultMsg = params.get("resultMsg");
        String tid = params.get("tid");
        String ordNo = params.get("ordNo");
        String goodsAmt = params.get("goodsAmt");
        String signData = params.get("signData");

        if ("0000".equals(resultCode)) {
            // 인증 성공 - PDF 3.1.3에 따라 승인 요청
            try {
                Map<String, String> approvalResult = requestApproval(params);

                if ("0000".equals(approvalResult.get("resultCd"))) {
                    // 승인 성공
                    model.addAttribute("success", true);
                    model.addAttribute("message", "결제가 완료되었습니다.");
                    model.addAttribute("ordNo", ordNo);
                    model.addAttribute("amt", approvalResult.get("amt"));
                    model.addAttribute("appNo", approvalResult.get("appNo"));
                } else {
                    // 승인 실패
                    model.addAttribute("success", false);
                    model.addAttribute("message", "결제 승인 실패: " + approvalResult.get("resultMsg"));
                }
            } catch (Exception e) {
                model.addAttribute("success", false);
                model.addAttribute("message", "승인 요청 오류: " + e.getMessage());
            }
        } else {
            // 인증 실패
            model.addAttribute("success", false);
            model.addAttribute("message", "결제 인증 실패: " + resultMsg);
        }

        return "payment-result";
    }

    /**
     * PDF 3.1.3에서 명시된 승인 요청
     */
    private Map<String, String> requestApproval(Map<String, String> authResult) throws Exception {
        System.out.println("=== Payster 승인 요청 시작 ===");

        String tid = authResult.get("tid");
        String ediDate = authResult.get("ediDate");
        String mid = authResult.get("mid");
        String goodsAmt = authResult.get("goodsAmt");
        String signData = authResult.get("signData");

        // PDF에서 명시된 encData 생성 (승인 시에도 동일한 방식)
        String encData = generateSHA256Hash(mid + ediDate + goodsAmt + MERCHANT_KEY);

        // PDF 3.1.3에서 명시된 승인 요청 필드
        MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
        requestData.add("tid", tid);
        requestData.add("ediDate", ediDate);
        requestData.add("mid", mid);
        requestData.add("goodsAmt", goodsAmt);
        requestData.add("charSet", "UTF-8");
        requestData.add("encData", encData);
        requestData.add("signData", signData);

        // HTTP 요청 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Charset", "UTF-8");

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(requestData, headers);

        System.out.println("승인 요청 URL: " + PAYMENT_APPROVE_URL);
        System.out.println("승인 요청 데이터: " + requestData);

        // Payster 승인 API 호출
        ResponseEntity<String> response = restTemplate.postForEntity(
                PAYMENT_APPROVE_URL, requestEntity, String.class);

        System.out.println("승인 응답: " + response.getBody());

        // 응답 파싱 (key=value&key=value 형태)
        return parseResponse(response.getBody());
    }

    /**
     * PDF 3.2에서 명시된 결제 취소
     */
    @PostMapping("/cancel")
    @ResponseBody
    public Map<String, Object> cancelPayment(@RequestParam String ordNo,
                                             @RequestParam String canAmt,
                                             @RequestParam String canMsg) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 실제로는 DB에서 주문 정보를 가져와야 함
            String tid = "실제_거래번호"; // DB에서 조회
            String ediDate = getCurrentEdiDate();

            // PDF 3.2.1에서 명시된 취소 요청 필드
            MultiValueMap<String, String> requestData = new LinkedMultiValueMap<>();
            requestData.add("tid", tid);
            requestData.add("ordNo", ordNo);
            requestData.add("canAmt", canAmt);
            requestData.add("canMsg", canMsg);
            requestData.add("canIp", "127.0.0.1");
            requestData.add("partCanFlg", "0"); // 전체취소
            requestData.add("ediDate", ediDate);

            // encData 생성
            String encData = generateSHA256Hash(MID + ediDate + canAmt + MERCHANT_KEY);
            requestData.add("encData", encData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity =
                    new HttpEntity<>(requestData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    PAYMENT_CANCEL_URL, requestEntity, String.class);

            Map<String, String> cancelResult = parseResponse(response.getBody());

            result.put("success", "0000".equals(cancelResult.get("resultCd")));
            result.put("message", cancelResult.get("resultMsg"));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "취소 요청 오류: " + e.getMessage());
        }

        return result;
    }

    /**
     * PDF 7.1에서 명시된 SHA256 암호화
     * encData = Hex(SHA256(mid + ediDate + goodsAmt + merchantKey))
     */
    private String generateSHA256Hash(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toLowerCase();
    }

    /**
     * PDF에서 요구하는 ediDate 형식 (yyyymmddhhmmss)
     */
    private String getCurrentEdiDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    /**
     * Payster 응답 파싱 (key=value&key=value 형태)
     */
    private Map<String, String> parseResponse(String responseBody) {
        Map<String, String> result = new HashMap<>();

        if (responseBody != null && !responseBody.trim().isEmpty()) {
            String[] pairs = responseBody.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = keyValue[0];
                        String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                        result.put(key, value);
                    } catch (Exception e) {
                        System.err.println("응답 파싱 오류: " + pair);
                    }
                }
            }
        }

        return result;
    }
}