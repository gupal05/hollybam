// src/main/java/com/hollybam/hollybam/services/IF_OrderService.java
package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

public interface IF_OrderService {
    OrderDto createOrderFromCart(Map<String, Object> orderData) throws Exception;
    OrderDto createDirectOrder(Map<String, Object> orderData) throws Exception;
    OrderDto getOrderDetail(String orderId) throws Exception;
    List<OrderDto> getMemberOrders(int memCode, int page, int size) throws Exception;
    List<OrderDto> getGuestOrders(int guestCode, int page, int size) throws Exception;
    boolean updateOrderStatus(int orderCode, String orderStatus) throws Exception;
    boolean updatePaymentStatus(int orderCode, String paymentStatus, String pgProvider, String pgTid) throws Exception;
    boolean cancelOrder(int orderCode, String orderId) throws Exception;
    String generateOrderId();
    int getMemberOrderCount(int memCode);
    int getGuestOrderCount(int guestCode);
    List<Map<String, Object>> getOrderDetails(int orderCode);
    List<OrderDto> selectOrdersByMember(int memberCode);
    List<OrderDto> selectOrdersByGuest(int guestCode);
    List<OrderDto> selectOrdersByMemberForLimit(int memberCode);
    List<OrderDto> selectOrdersByGuestForLimit(int guestCode);
    int getFinalAmount(int code);
    DeliveryDto getTrackingNumber(int orderCode);
    OrderItemDto getOrderItemDetail(int orderItemCode);
    // IF_OrderService.java 인터페이스에도 추가해야 할 메서드 선언:
    /**
     * 위시리스트에서 선택한 여러 상품으로 임시 주문 생성
     * @param orderData 주문 데이터 (주문자 정보 + 상품 리스트)
     * @return 생성된 주문 정보
     * @throws Exception 주문 생성 실패 시
     */
    OrderDto createTempOrder(Map<String, Object> orderData) throws Exception;
    /**
     * 주문 완료 후 적립금 처리 (사용 + 적립)
     * @param orderCode 주문 코드
     * @param memCode 회원 코드 (회원 주문인 경우만)
     * @param usePoints 사용한 적립금
     * @param finalAmount 최종 결제 금액
     */
    void processOrderPoints(int orderCode, Integer memCode, int usePoints, int finalAmount) throws Exception;
    String getProductName(int productCode);

    void updatePaymentStatus(String orderId, String status);

    Map<String, Object> getCartProductName(int orderCode);
    OrderDto createOrderByBank(Map<String, Object> orderData, HttpSession session) throws Exception;
    OrderDto createDirectOrderByTrans(Map<String, Object> orderData, HttpSession session) throws Exception;
}