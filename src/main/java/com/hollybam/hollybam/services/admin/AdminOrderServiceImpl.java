package com.hollybam.hollybam.services.admin;

import com.hollybam.hollybam.dao.admin.IF_AdminOrderDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AdminOrderServiceImpl implements IF_AdminOrderService {
    @Autowired
    private IF_AdminOrderDao adminOrderDao;

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
    public Map<String, Object>selectOrderDetail(String orderId) {
        return adminOrderDao.selectOrderDetail(orderId);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> selectOrderItemsByOrderCode(int orderCode) {
        return adminOrderDao.selectOrderItemsByOrderCode(orderCode);
    }
}
