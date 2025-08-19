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
    void updateShippingStatus(@Param("orderCodes") List<Integer> orderCodes);
    void updateDeliveredStatus(@Param("orderCodes") List<Integer> orderCodes);
}
