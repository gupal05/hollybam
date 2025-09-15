package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminOrderDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminOrderServiceImpl implements IF_AdminOrderService {
    @Autowired
    private IF_AdminOrderDao adminOrderDao;

    @Override
    @Transactional
    public int getTotalOrderCount(){
        return adminOrderDao.getTotalOrderCount();
    }

    @Override
    @Transactional
    public int getPendingOrderCount(){
        return adminOrderDao.getPendingOrderCount();
    }

    @Override
    @Transactional
    public int getPaidOrderCount(){
        return adminOrderDao.getPaidOrderCount();
    }

    @Override
    @Transactional
    public int getOrderPendingOrderCount(){
        return adminOrderDao.getOrderPendingOrderCount();
    }

    @Override
    @Transactional
    public int getShippingOrderCount(){
        return adminOrderDao.getShippingOrderCount();
    }

    @Override
    @Transactional
    public int getDeliveryOrderCount(){
        return adminOrderDao.getDeliveryOrderCount();
    }

    @Override
    @Transactional
    public Map<String, Object> getOrderCounts() {
        return adminOrderDao.getOrderCounts();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> selectOrderSummaryWithStatus() {
        return adminOrderDao.selectOrderSummaryWithStatus();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListPending(){
        return adminOrderDao.getOrderListPending();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListPaid(){
        return adminOrderDao.getOrderListPaid();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListOrderPending(){
        return adminOrderDao.getOrderListOrderPending();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListShipping(){
        return adminOrderDao.getOrderListShipping();
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderListDelivered(){
        return adminOrderDao.getOrderListDelivered();
    }

    @Override
    @Transactional
    public Map<String, Object>selectOrderDetail(String orderId) {
        return adminOrderDao.selectOrderDetail(orderId);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> selectOrderItemsByOrderCode(int orderCode) {
        return adminOrderDao.selectOrderItemsByOrderCode(orderCode);
    }

    @Override
    @Transactional
    public Map<String, Object> getDiscountType(int orderCode) {
        return adminOrderDao.getDiscountType(orderCode);
    }

    @Override
    @Transactional
    public int getOrderCodeByOrderId(String orderId){
        return adminOrderDao.getOrderCodeByOrderId(orderId);
    }

    @Override
    @Transactional
    public int getDeliveryStatusCount(int orderCode){
        return adminOrderDao.getDeliveryStatusCount(orderCode);
    }

    @Override
    @Transactional
    public void updateDeliveryStatusNull(List<Integer> orderCodes){
        adminOrderDao.updateDeliveryStatusNull(orderCodes);
    }

    @Override
    @Transactional
    public void updatePayPendingStatus(List<Integer> orderCodes){
        adminOrderDao.updatePayPendingStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updatePaidStatus(List<Integer> orderCodes){
        adminOrderDao.updatePaidStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updateOrderPendingStatus(List<Integer> orderCodes){
        adminOrderDao.updateOrderPendingStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updateShippingStatus(List<Map<String, Object>> orders){
        for(int i=0; i<orders.size();i++){
            orders.get(i).put("deliveryMemo", adminOrderDao.getDeliveryMemo(Integer.parseInt(orders.get(i).get("orderCode").toString())));
        }
        adminOrderDao.insertDeliveryStatus(orders);
        List<Integer> orderCodes = new ArrayList<Integer>();
        for(int i=0; i<orders.size();i++){
            orderCodes.add(Integer.parseInt(orders.get(i).get("orderCode").toString()));
        }
        adminOrderDao.updateShippingStatus(orderCodes);
    }

    @Override
    @Transactional
    public void updateDeliveredStatus(List<Integer> orderCodes){
        adminOrderDao.updateDeliveredStatus(orderCodes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> searchOrdersWithConditions(Map<String, Object> searchParams) {
        try {
            log.info("검색 조건으로 주문 목록 조회: {}", searchParams);

            // 페이지네이션 처리
            int page = (Integer) searchParams.getOrDefault("page", 1);
            int size = (Integer) searchParams.getOrDefault("size", 20);
            int offset = (page - 1) * size;

            // 검색 파라미터에 offset, limit 추가
            searchParams.put("offset", offset);
            searchParams.put("limit", size);

            // DAO 호출
            List<Map<String, Object>> result = adminOrderDao.searchOrdersWithConditions(searchParams);

            log.info("검색된 주문 개수: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("검색 조건으로 주문 목록 조회 중 오류 발생", e);
            throw new RuntimeException("주문 검색 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getSearchOrderCount(Map<String, Object> searchParams) {
        try {
            log.info("검색 조건으로 주문 개수 조회: {}", searchParams);

            int count = adminOrderDao.getSearchOrderCount(searchParams);

            log.info("검색된 총 주문 개수: {}", count);
            return count;

        } catch (Exception e) {
            log.error("검색 조건으로 주문 개수 조회 중 오류 발생", e);
            throw new RuntimeException("주문 개수 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countOrdersTotal(){
        return adminOrderDao.countOrdersTotal();
    }

}
