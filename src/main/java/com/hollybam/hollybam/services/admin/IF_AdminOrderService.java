package com.hollybam.hollybam.services.admin;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IF_AdminOrderService {
    Map<String, Object> getOrderCounts();
    int getTotalOrderCount();
    int getPendingOrderCount();
    int getPaidOrderCount();
    int getOrderPendingOrderCount();
    int getShippingOrderCount();
    int getDeliveryOrderCount();
    List<Map<String, Object>> selectOrderSummaryWithStatus();
    List<Map<String, Object>> getOrderListPending();
    List<Map<String, Object>> getOrderListPaid();
    List<Map<String, Object>> getOrderListOrderPending();
    List<Map<String, Object>> getOrderListShipping();
    List<Map<String, Object>> getOrderListDelivered();
    Map<String, Object>selectOrderDetail(String orderId);
    List<Map<String, Object>> selectOrderItemsByOrderCode(int orderCode);
    Map<String, Object> getDiscountType(int orderCode);
    int getOrderCodeByOrderId(String orderId);
    int getDeliveryStatusCount(@Param("orderCode") int orderCode);
    void updateDeliveryStatusNull(@Param("orderCodes") List<Integer> orderCodes);
    void updatePayPendingStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updatePaidStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updateOrderPendingStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updateShippingStatus(List<Map<String, Object>> orders);
    void updateDeliveredStatus(@Param("orderCodes") List<Integer> orderCodes);
    /**
     * 검색 조건에 따른 주문 목록 조회
     * @param searchParams 검색 조건 맵
     *                    - status: 주문 상태 (ALL, payPending, paid, orderPending, shipping, delivered)
     *                    - orderIdFilter: 주문번호 필터
     *                    - ordererNameFilter: 주문자명 필터
     *                    - productNameFilter: 상품명 필터
     *                    - startDate: 검색 시작일 (yyyy-MM-dd)
     *                    - endDate: 검색 종료일 (yyyy-MM-dd)
     *                    - paymentMethodFilter: 결제방법 필터
     *                    - page: 페이지 번호
     *                    - size: 페이지 크기
     * @return 검색된 주문 목록
     */
    List<Map<String, Object>> searchOrdersWithConditions(Map<String, Object> searchParams);

    /**
     * 검색 조건에 따른 주문 개수 조회
     * @param searchParams 검색 조건 맵
     * @return 검색된 주문 개수
     */
    int getSearchOrderCount(Map<String, Object> searchParams);

    int countOrdersTotal();
}
