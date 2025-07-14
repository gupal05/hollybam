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
}