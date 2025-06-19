package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_PaymentDao;
import com.hollybam.hollybam.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PaymentService implements IF_PaymentService {

    @Autowired
    private IF_PaymentDao paymentDao;

    // properties 파일의 키 이름과 일치하도록 수정
    @Value("${portone.api_key}")
    private String impKey;

    @Value("${portone.api_secret}")
    private String impSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public String preparePayment(PaymentRequestDto paymentRequestDto) {
        try {
            // 1. 성인인증 검증
            if (!verifyAdultCertification(paymentRequestDto.getMemCode(), paymentRequestDto.getGuestUuid())) {
                throw new RuntimeException("성인인증이 필요합니다.");
            }

            // 2. 필수 약관 동의 검증
            if (!paymentRequestDto.isAgreeTerms() || !paymentRequestDto.isAgreePrivacy()) {
                throw new RuntimeException("필수 약관에 동의해주세요.");
            }

            // 3. 장바구니 정보 조회 및 검증
            List<CartDto> cartItems = paymentDao.selectCartItemsWithDetails(paymentRequestDto.getCartCodes());
            if (cartItems.isEmpty()) {
                throw new RuntimeException("주문할 상품이 없습니다.");
            }

            // 4. 재고 검증
            for (CartDto cartItem : cartItems) {
                if (cartItem.getOptionCode() != null) {
                    int optionStock = paymentDao.selectOptionStock(cartItem.getOptionCode());
                    if (optionStock < cartItem.getQuantity()) {
                        throw new RuntimeException(cartItem.getProductDto().getProductName() + " 옵션의 재고가 부족합니다.");
                    }
                } else {
                    int productStock = paymentDao.selectProductStock(cartItem.getProductCode());
                    if (productStock < cartItem.getQuantity()) {
                        throw new RuntimeException(cartItem.getProductDto().getProductName() + "의 재고가 부족합니다.");
                    }
                }
            }

            // 5. 주문 생성
            String orderId = generateOrderId();
            OrderDto orderDto = createOrderFromRequest(paymentRequestDto, orderId, cartItems);

            int result = paymentDao.insertOrder(orderDto);
            if (result <= 0) {
                throw new RuntimeException("주문 생성에 실패했습니다.");
            }

            // 6. 주문 상품 등록
            List<OrderItemDto> orderItems = createOrderItems(orderDto.getOrderCode(), cartItems);
            paymentDao.insertOrderItems(orderItems);

            // 7. 결제 로그 저장
            PaymentLogDto paymentLog = new PaymentLogDto();
            paymentLog.setOrderId(orderId);
            paymentLog.setMerchantUid(orderId);
            paymentLog.setAmount(paymentRequestDto.getFinalAmount());
            paymentLog.setStatus("PREPARED");
            paymentLog.setAdultVerified(paymentRequestDto.isAdultVerified());
            paymentLog.setBuyerBirth(paymentRequestDto.getBuyerBirth());
            paymentLog.setVerificationMethod("SYSTEM_VERIFIED");
            savePaymentLog(paymentLog);

            return orderId;

        } catch (Exception e) {
            throw new RuntimeException("결제 준비 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto completePayment(String impUid, String orderId) {
        PaymentResponseDto response = new PaymentResponseDto();

        try {
            // 1. 아임포트에서 결제 정보 조회
            String accessToken = getAccessToken();
            JsonNode paymentData = getPaymentFromPortOne(accessToken, impUid);

            if (paymentData == null) {
                throw new RuntimeException("결제 정보를 찾을 수 없습니다.");
            }

            // 2. 주문 정보 조회
            OrderDto order = paymentDao.selectOrderByOrderId(orderId);
            if (order == null) {
                throw new RuntimeException("주문 정보를 찾을 수 없습니다.");
            }

            // 3. 결제 금액 검증
            int paidAmount = paymentData.get("amount").asInt();
            if (paidAmount != order.getFinalAmount()) {
                throw new RuntimeException("결제 금액이 일치하지 않습니다.");
            }

            // 4. 결제 상태 확인
            String status = paymentData.get("status").asText();
            if (!"paid".equals(status)) {
                throw new RuntimeException("결제가 완료되지 않았습니다.");
            }

            // 5. 재고 차감
            List<OrderItemDto> orderItems = paymentDao.selectOrderItemsByOrderCode(order.getOrderCode());
            if (!reduceStock(orderItems)) {
                throw new RuntimeException("재고 차감에 실패했습니다.");
            }

            // 6. 주문 상태 업데이트
            paymentDao.updateOrderStatus(orderId, "PAID");
            paymentDao.updatePaymentInfo(orderId, impUid,
                    paymentData.get("pg_provider").asText(),
                    paymentData.get("pg_tid").asText(),
                    "PAID");

            // 7. 결제 로그 업데이트
            PaymentLogDto paymentLog = new PaymentLogDto();
            paymentLog.setOrderId(orderId);
            paymentLog.setImpUid(impUid);
            paymentLog.setMerchantUid(orderId);
            paymentLog.setAmount(paidAmount);
            paymentLog.setStatus("PAID");
            paymentLog.setPgProvider(paymentData.get("pg_provider").asText());
            paymentLog.setPgTid(paymentData.get("pg_tid").asText());
            paymentLog.setPayMethod(paymentData.get("pay_method").asText());
            paymentLog.setAdultVerified(true);
            paymentLog.setResponseData(paymentData.toString());
            savePaymentLog(paymentLog);

            response.setSuccess(true);
            response.setMessage("결제가 완료되었습니다.");
            response.setOrderId(orderId);
            response.setImpUid(impUid);
            response.setAmount(paidAmount);

        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            response.setErrorMsg(e.getMessage());

            // 실패 시 재고 복구 및 주문 상태 업데이트
            try {
                paymentDao.updateOrderStatus(orderId, "CANCELLED");
                paymentDao.updatePaymentInfo(orderId, impUid, null, null, "FAILED");

                PaymentLogDto failLog = new PaymentLogDto();
                failLog.setOrderId(orderId);
                failLog.setImpUid(impUid);
                failLog.setMerchantUid(orderId);
                failLog.setStatus("FAILED");
                failLog.setErrorMsg(e.getMessage());
                savePaymentLog(failLog);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return response;
    }

    @Override
    public void failPayment(String orderId, String errorMsg) {
        try {
            paymentDao.updateOrderStatus(orderId, "CANCELLED");

            PaymentLogDto paymentLog = new PaymentLogDto();
            paymentLog.setOrderId(orderId);
            paymentLog.setMerchantUid(orderId);
            paymentLog.setStatus("FAILED");
            paymentLog.setErrorMsg(errorMsg);
            savePaymentLog(paymentLog);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public OrderDto getOrder(String orderId) {
        return paymentDao.selectOrderByOrderId(orderId);
    }

    @Override
    public List<OrderDto> getOrdersByMember(int memCode) {
        return paymentDao.selectOrdersByMember(memCode);
    }

    @Override
    public List<OrderDto> getOrdersByGuest(String guestUuid) {
        int guestCode = paymentDao.selectGuestCodeByUuid(guestUuid);
        return paymentDao.selectOrdersByGuest(guestCode);
    }

    @Override
    public PaymentRequestDto calculateOrderFromCart(List<Integer> cartCodes, Integer memCode, String guestUuid) {
        List<CartDto> cartItems = paymentDao.selectCartItemsWithDetails(cartCodes);

        PaymentRequestDto paymentRequest = new PaymentRequestDto();
        paymentRequest.setCartCodes(cartCodes);
        paymentRequest.setMemCode(memCode);
        paymentRequest.setGuestUuid(guestUuid);

        int totalAmount = 0;
        for (CartDto cartItem : cartItems) {
            int itemPrice = cartItem.getPriceDto().getPriceSelling();
            if (cartItem.getOptionCode() != null) {
                itemPrice += cartItem.getProductOptionDto().getOptionPrice();
            }
            totalAmount += itemPrice * cartItem.getQuantity();
        }

        paymentRequest.setTotalAmount(totalAmount);
        paymentRequest.setDiscountAmount(0); // 할인 로직 추가 가능
        paymentRequest.setDeliveryFee(totalAmount >= 50000 ? 0 : 3000); // 5만원 이상 무료배송
        paymentRequest.setFinalAmount(totalAmount - paymentRequest.getDiscountAmount() + paymentRequest.getDeliveryFee());

        return paymentRequest;
    }

    @Override
    public boolean verifyAdultCertification(Integer memCode, String guestUuid) {
        System.out.println(memCode);
        System.out.println(paymentDao.isAdultVerifiedMember(memCode));
        if (memCode != null) {
            return paymentDao.isAdultVerifiedMember(memCode);
        } else if (guestUuid != null) {
            int guestCode = paymentDao.selectGuestCodeByUuid(guestUuid);
            return paymentDao.isAdultVerifiedGuest(guestCode);
        }
        return false;
    }

    @Override
    public void savePaymentLog(PaymentLogDto paymentLogDto) {
        paymentDao.insertPaymentLog(paymentLogDto);
    }

    @Override
    @Transactional
    public boolean reduceStock(List<OrderItemDto> orderItems) {
        try {
            for (OrderItemDto item : orderItems) {
                if (item.getOptionCode() != null) {
                    paymentDao.updateOptionStock(item.getOptionCode(), -item.getQuantity());
                } else {
                    paymentDao.updateProductStock(item.getProductCode(), -item.getQuantity());
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public void restoreStock(List<OrderItemDto> orderItems) {
        try {
            for (OrderItemDto item : orderItems) {
                if (item.getOptionCode() != null) {
                    paymentDao.updateOptionStock(item.getOptionCode(), item.getQuantity());
                } else {
                    paymentDao.updateProductStock(item.getProductCode(), item.getQuantity());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCartItems(List<Integer> cartCodes) {
        paymentDao.deleteCartItems(cartCodes);
    }

    // Private helper methods
    private String generateOrderId() {
        return "ORDER_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderDto createOrderFromRequest(PaymentRequestDto request, String orderId, List<CartDto> cartItems) {
        OrderDto order = new OrderDto();
        order.setOrderId(orderId);
        order.setMemCode(request.getMemCode());

        if (request.getGuestUuid() != null) {
            order.setGuestCode(paymentDao.selectGuestCodeByUuid(request.getGuestUuid()));
        }

        // 주문자 정보
        order.setOrdererName(request.getOrdererName());
        order.setOrdererPhone(request.getOrdererPhone());
        order.setOrdererEmail(request.getOrdererEmail());

        // 배송 정보
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverZip(request.getReceiverZip());
        order.setReceiverAddr(request.getReceiverAddr());
        order.setReceiverAddrDetail(request.getReceiverAddrDetail());
        order.setDeliveryRequest(request.getDeliveryRequest());

        // 결제 정보
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTotalAmount(request.getTotalAmount());
        order.setDiscountAmount(request.getDiscountAmount());
        order.setDeliveryFee(request.getDeliveryFee());
        order.setFinalAmount(request.getFinalAmount());

        // 상태
        order.setOrderStatus("PENDING");
        order.setPaymentStatus("PENDING");

        // 성인인증
        order.setAdultVerified(request.isAdultVerified());
        order.setAdultVerifiedAt(LocalDateTime.now());

        return order;
    }

    private List<OrderItemDto> createOrderItems(int orderCode, List<CartDto> cartItems) {
        return cartItems.stream().map(cartItem -> {
            OrderItemDto orderItem = new OrderItemDto();
            orderItem.setOrderCode(orderCode);
            orderItem.setProductCode(cartItem.getProductCode());
            orderItem.setOptionCode(cartItem.getOptionCode());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPriceDto().getPriceSelling());
            orderItem.setOptionPrice(cartItem.getOptionCode() != null ? cartItem.getProductOptionDto().getOptionPrice() : 0);
            orderItem.setTotalPrice((orderItem.getUnitPrice() + orderItem.getOptionPrice()) * orderItem.getQuantity());
            return orderItem;
        }).collect(java.util.stream.Collectors.toList());
    }

    // 수정된 getAccessToken 메서드
    private String getAccessToken() throws Exception {
        String url = "https://api.iamport.kr/users/getToken";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", impKey);         // 필드명을 수정함
        body.put("imp_secret", impSecret);   // 필드명을 수정함

        System.out.println("API Key: " + impKey);
        System.out.println("API Secret: " + impSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        System.out.println("Response: " + response.getBody());

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        if (jsonNode.get("code").asInt() == 0) {
            return jsonNode.get("response").get("access_token").asText();
        } else {
            throw new RuntimeException("아임포트 토큰 발급 실패: " + jsonNode.get("message").asText());
        }
    }

    private JsonNode getPaymentFromPortOne(String accessToken, String impUid) throws Exception {
        String url = "https://api.iamport.kr/payments/" + impUid;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        if (jsonNode.get("code").asInt() == 0) {
            return jsonNode.get("response");
        } else {
            throw new RuntimeException("결제 정보 조회 실패: " + jsonNode.get("message").asText());
        }
    }

    @Override
    public List<CartDto> getCartItemsWithDetails(List<Integer> cartCodes) {
        try {
            return paymentDao.selectCartItemsWithDetails(cartCodes);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}