package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminOrderDao {
    Map<String, Object> getOrderCounts();
    List<Map<String, Object>> selectOrderSummaryWithStatus();
    Map<String, Object>selectOrderDetail(@Param("orderId") String orderId);
    List<Map<String, Object>> selectOrderItemsByOrderCode(@Param("orderCode") int orderCode);
}
