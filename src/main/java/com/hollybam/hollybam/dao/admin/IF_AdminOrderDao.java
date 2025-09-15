package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminOrderDao {
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
    Map<String, Object>selectOrderDetail(@Param("orderId") String orderId);
    List<Map<String, Object>> selectOrderItemsByOrderCode(@Param("orderCode") int orderCode);
    Map<String, Object> getDiscountType(@Param("orderCode") int orderCode);
    int getOrderCodeByOrderId(@Param("orderId") String orderId);
    int getDeliveryStatusCount(@Param("orderCode") int orderCode);
    void updateDeliveryStatusNull(@Param("orderCodes") List<Integer> orderCodes);
    void updatePayPendingStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updatePaidStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updateOrderPendingStatus(@Param("orderCodes") List<Integer> orderCodes);
    String getDeliveryMemo(@Param("orderCode") int orderCode);
    void insertDeliveryStatus(@Param("orders") List<Map<String, Object>> orders);
    void updateShippingStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updateDeliveredStatus(@Param("orderCodes") List<Integer> orderCodes);

    // 🆕 검색 기능 메서드 추가
    /**
     * 검색 조건에 따른 주문 목록 조회
     * @param searchParams 검색 조건 맵
     * @return 검색된 주문 목록
     */
    List<Map<String, Object>> searchOrdersWithConditions(@Param("searchParams") Map<String, Object> searchParams);

    /**
     * 검색 조건에 따른 주문 개수 조회
     * @param searchParams 검색 조건 맵
     * @return 검색된 주문 개수
     */
    int getSearchOrderCount(@Param("searchParams") Map<String, Object> searchParams);

    int countOrdersTotal();
}
