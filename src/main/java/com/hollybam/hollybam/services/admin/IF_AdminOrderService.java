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
    void updateShippingStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updateDeliveredStatus(@Param("orderCodes") List<Integer> orderCodes);
}
