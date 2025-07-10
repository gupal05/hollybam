// src/main/java/com/hollybam/hollybam/dao/IF_OrderDao.java
package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface IF_OrderDao {
    // 주문 등록
    int insertOrder(OrderDto orderDto);

    // 주문 상품 등록
    int insertOrderItems(@Param("orderItems") List<OrderItemDto> orderItems);

    int updateOrderCount(OrderItemDto orderItems);

    // 배송 정보 등록
    int insertDelivery(DeliveryDto deliveryDto);

    // 주문 상세 조회
    OrderDto selectOrderByOrderId(@Param("orderId") String orderId);

    // 회원 주문 목록 조회
    List<OrderDto> selectOrdersByMemberCode(@Param("memCode") int memCode, @Param("offset") int offset, @Param("limit") int limit);

    // 비회원 주문 목록 조회
    List<OrderDto> selectOrdersByGuestCode(@Param("guestCode") int guestCode, @Param("offset") int offset, @Param("limit") int limit);

    // 주문 상품 목록 조회
    List<OrderItemDto> selectOrderItemsByOrderCode(@Param("orderCode") int orderCode);

    // 주문 상태 업데이트
    int updateOrderStatus(@Param("orderCode") int orderCode, @Param("orderStatus") String orderStatus);

    // 결제 상태 업데이트
    int updatePaymentStatus(@Param("orderCode") int orderCode, @Param("paymentStatus") String paymentStatus, @Param("pgProvider") String pgProvider, @Param("pgTid") String pgTid);

    // 회원 주문 개수 조회
    int countOrdersByMemberCode(@Param("memCode") int memCode);

    // 비회원 주문 개수 조회
    int countOrdersByGuestCode(@Param("guestCode") int guestCode);

    // 재고 관리 (수정된 버전)
    int updateProductQuantity(@Param("productCode") int productCode, @Param("quantity") int quantity);
    int updateOptionQuantity(@Param("optionCode") int optionCode, @Param("quantity") int quantity);
    int restoreProductQuantity(@Param("productCode") int productCode, @Param("quantity") int quantity);
    int restoreOptionQuantity(@Param("optionCode") int optionCode, @Param("quantity") int quantity);
    int updateProductTotalQuantityFromOptions(@Param("productCode") int productCode);

    // 장바구니 삭제
    int deleteCartItems(@Param("cartCodes") List<Integer> cartCodes);

    // 주문 제품 리스트 조회
    List<Map<String, Object>> getOrderDetails(int orderCode);

    // 주문 조회
    List<OrderDto> selectOrdersByMember(int memberCode);
    List<OrderDto> selectOrdersByGuest(int guestCode);
    List<OrderDto> selectOrdersByMemberForLimit(int memberCode);
    List<OrderDto> selectOrdersByGuestForLimit(int guestCode);
    int getFinalAmount(int code);
    DeliveryDto getTrackingNumber(int orderCode);
}