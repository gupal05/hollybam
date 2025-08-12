package com.hollybam.hollybam.services.admin;

import java.util.List;
import java.util.Map;

public interface IF_AdminOrderService {
    Map<String, Object> getOrderCounts();
    List<Map<String, Object>> selectOrderSummaryWithStatus();
    Map<String, Object>selectOrderDetail(String orderId);
    List<Map<String, Object>> selectOrderItemsByOrderCode(int orderCode);
}
