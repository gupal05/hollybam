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
    OrderItemDto selectOrderItemDetail(int orderItemCode);
    String getProductName(int productCode);
    void updatePaymentStatus(@Param("orderId") String orderId, @Param("status") String status);

    Map<String, Object> getCartProductName(@Param("orderCode") int orderCode);

    /**
     * 🗑️ 주문 아이템 삭제
     */
    int deleteOrderItems(int orderCode);

    /**
     * 🗑️ 주문 삭제 (조건부)
     */
    int deleteOrder(int orderCode);

    /**
     * 🗑️ 즉시 주문 삭제 (조건 없음)
     */
    int deleteOrderInstant(int orderCode);

    /**
     * 🚀 삭제용 주문 코드 조회
     */
    Integer getOrderCodeByIdForDelete(String orderId);

    /**
     * 🚀 재고 복원용 주문 아이템 조회
     */
    List<Map<String, Object>> getOrderItemsForRestore(int orderCode);

    /**
     * 🔄 빠른 옵션 재고 복원
     */
    int restoreOptionQuantityFast(@Param("optionCode") int optionCode, @Param("quantity") int quantity);

    /**
     * 🔄 빠른 상품 재고 복원
     */
    int restoreProductQuantityFast(@Param("productCode") int productCode, @Param("quantity") int quantity);

    int getOrderCodeNumber(@Param("date") String date);

    List<Map<String, Object>> getOrderItemsList(@Param("orderCode") int orderCode);

    // 주문 금액/회원여부
    Map<String, Object> getOrderAmounts(@Param("orderCode") int orderCode);

    // 환불 배치 / 환불 아이템
    void insertRefundBatch(Map<String, Object> batch);
    void updateRefundBatchTotals(Map<String, Object> params);
    void insertRefundItem(Map<String, Object> item);

    // 제품/주문아이템
    int getProductCodeByProductId(@Param("productId") String productId);
    int getOrderItemsCodeByProductCode(@Param("orderCode") int orderCode,
                                       @Param("productCode") int productCode);
    Map<String, Object> getOrderItemPrice(@Param("orderItemCode") int orderItemCode);
    int countOrderItems(@Param("orderCode") int orderCode);

    // 쿠폰
    int isCouponUsedOrder(@Param("orderCode") int orderCode);
    int getCouponMemberCode(@Param("orderCode") int orderCode);
    void updateCouponMemberByRefund(Map<String, Object> params); // {couponMemberCode}
    CouponDto getUseCouponInfoByRefund(@Param("orderCode") int orderCode);
    void updateCouponMemberDiscountAmount(Map<String, Object> params); // {orderCode, discountAmount}

    // 할인코드
    int isDiscountCodeUsedOrder(@Param("orderCode") int orderCode);
    DiscountDto getUseDiscountInfoByRefund(@Param("orderCode") int orderCode);
    void delDiscountCodeUsageByRefund(@Param("orderCode") int orderCode);
    void updateDiscountCodeUsageAmount(Map<String, Object> params); // {orderCode, discountAmount}

    // 포인트
    int isOrderPoint(@Param("orderCode") int orderCode);
    List<Map<String, Object>> getPointInfo(@Param("orderCode") int orderCode);
    void deletePointsByCodes(List<Map<String, Object>> list);
    void insertPoint(PointDto pointDto);

    Map<String, Object> selectOrderAmounts(int orderCode);

    Map<String, Object> getOrderItemByProductId(Map<String, Object> param);

    int getRefundedQtyForOrderItem(int orderItemCode);

    List<Map<String,Object>> getOrderItemsForRefund(int orderCode);
    Map<String,Object> getOrderItemByCode(int orderItemCode);
    Integer sumRefundedQty(int orderItemCode);
    Map<String,Object> getOrderHeaderForRefund(int orderCode);

    /**
     * 환불 신청 시 주문 상태 업데이트
     * @param orderCode 주문 코드
     * @param orderStatus 새로운 주문 상태 (REFUND_PENDING 또는 RETURN_REQUESTED)
     * @param paymentStatus 새로운 결제 상태 (REFUND_PENDING)
     * @param simpleStatus 새로운 간단 상태 (CANCELLED)
     */
    void updateOrderStatusForRefund(@Param("orderCode") int orderCode,
                                    @Param("orderStatus") String orderStatus,
                                    @Param("paymentStatus") String paymentStatus,
                                    @Param("simpleStatus") String simpleStatus);

    /**
     * 환불 신청 중복 체크
     * @param orderCode 주문 코드
     * @return 환불 배치 개수
     */
    int countRefundBatchesByOrder(@Param("orderCode") int orderCode);
}