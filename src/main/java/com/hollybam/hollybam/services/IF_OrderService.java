// src/main/java/com/hollybam/hollybam/services/IF_OrderService.java
package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.*;
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
}