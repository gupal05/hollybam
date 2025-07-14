// src/main/java/com/hollybam/hollybam/services/OrderServiceImpl.java
package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_OrderDao;
import com.hollybam.hollybam.dao.IF_PaymentDao;
import com.hollybam.hollybam.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements IF_OrderService {

    @Autowired
    private IF_OrderDao orderDao;

    @Autowired
    private IF_PaymentDao paymentDao;

    @Override
    @Transactional
    public OrderDto createOrderFromCart(Map<String, Object> orderData) throws Exception {
        try {
            log.info("장바구니 주문 생성 시작: {}", orderData);

            @SuppressWarnings("unchecked")
            List<Integer> cartCodes = (List<Integer>) orderData.get("cartCodes");
            List<CartDto> cartItems = paymentDao.selectCartItemsWithDetails(cartCodes);

            if (cartItems.isEmpty()) {
                throw new Exception("주문할 상품이 없습니다.");
            }

            OrderDto order = createOrderFromData(orderData, cartItems);
            orderDao.insertOrder(order);
            log.info("주문 저장 완료. 주문코드: {}", order.getOrderCode());

            List<OrderItemDto> orderItems = createOrderItemsFromCart(order.getOrderCode(), cartItems);
            orderDao.insertOrderItems(orderItems);
            for(OrderItemDto orderItem : orderItems) {
                orderDao.updateOrderCount(orderItem);
            }

            // 수정된 재고 차감 로직
            updateInventory(orderItems);

            orderDao.deleteCartItems(cartCodes);
            createInitialDelivery(order.getOrderCode());

            log.info("장바구니 주문 생성 완료: {}", order.getOrderId());
            return order;

        } catch (Exception e) {
            log.error("장바구니 주문 생성 실패", e);
            throw new Exception("주문 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OrderDto createDirectOrder(Map<String, Object> orderData) throws Exception {
        try {
            log.info("바로 구매 주문 생성 시작: {}", orderData);

            int productCode = (Integer) orderData.get("productCode");
            Integer optionCode = (Integer) orderData.get("optionCode");
            int quantity = (Integer) orderData.get("quantity");

            PriceDto priceDto = paymentDao.selectProductPrice(productCode);
            ProductOptionDto optionDto = null;
            if (optionCode != null) {
                optionDto = paymentDao.selectProductOption(optionCode);
            }

            OrderDto order = createOrderFromData(orderData, null);
            orderDao.insertOrder(order);
            log.info("바로 구매 주문 저장 완료. 주문코드: {}", order.getOrderCode());

            List<OrderItemDto> orderItems = createDirectOrderItems(order.getOrderCode(), productCode, optionCode, quantity, priceDto, optionDto);
            orderDao.insertOrderItems(orderItems);
            for(OrderItemDto orderItem : orderItems) {
                orderDao.updateOrderCount(orderItem);
            }

            // 수정된 재고 차감 로직
            updateInventory(orderItems);

            createInitialDelivery(order.getOrderCode());

            log.info("바로 구매 주문 생성 완료: {}", order.getOrderId());
            return order;

        } catch (Exception e) {
            log.error("바로 구매 주문 생성 실패", e);
            throw new Exception("주문 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // 수정된 재고 차감 로직
    private void updateInventory(List<OrderItemDto> orderItems) throws Exception {
        for (OrderItemDto item : orderItems) {
            if (item.getOptionCode() != null) {
                // 옵션이 있는 상품: 옵션 재고만 차감
                int result = orderDao.updateOptionQuantity(item.getOptionCode(), item.getQuantity());
                if (result == 0) {
                    throw new Exception("옵션 재고가 부족합니다. 옵션 코드: " + item.getOptionCode());
                }
                // 상품 전체 재고 업데이트 (옵션 재고 합계로 재계산)
                orderDao.updateProductTotalQuantityFromOptions(item.getProductCode());
            } else {
                // 옵션이 없는 상품: 상품 재고만 차감
                int result = orderDao.updateProductQuantity(item.getProductCode(), item.getQuantity());
                if (result == 0) {
                    throw new Exception("상품 재고가 부족합니다. 상품 코드: " + item.getProductCode());
                }
            }
        }
    }

    // 수정된 재고 복원 로직
    private void restoreInventory(int orderCode) throws Exception {
        List<OrderItemDto> orderItems = orderDao.selectOrderItemsByOrderCode(orderCode);

        for (OrderItemDto item : orderItems) {
            if (item.getOptionCode() != null) {
                // 옵션이 있는 상품: 옵션 재고만 복원
                int result = orderDao.restoreOptionQuantity(item.getOptionCode(), item.getQuantity());
                if (result == 0) {
                    log.warn("옵션 재고 복원 실패: 옵션 코드 = {}, 수량 = {}", item.getOptionCode(), item.getQuantity());
                }
                // 상품 전체 재고 업데이트 (옵션 재고 합계로 재계산)
                orderDao.updateProductTotalQuantityFromOptions(item.getProductCode());
            } else {
                // 옵션이 없는 상품: 상품 재고만 복원
                int result = orderDao.restoreProductQuantity(item.getProductCode(), item.getQuantity());
                if (result == 0) {
                    log.warn("상품 재고 복원 실패: 상품 코드 = {}, 수량 = {}", item.getProductCode(), item.getQuantity());
                }
            }
        }
    }

    private OrderDto createOrderFromData(Map<String, Object> orderData, List<CartDto> cartItems) throws Exception {
        OrderDto order = new OrderDto();
        order.setOrderId(generateOrderId());

        if (orderData.get("memCode") != null) {
            order.setMemCode((Integer) orderData.get("memCode"));
        }
        if (orderData.get("guestCode") != null) {
            order.setGuestCode((Integer) orderData.get("guestCode"));
        }

        order.setOrdererName((String) orderData.get("ordererName"));
        order.setOrdererPhone((String) orderData.get("ordererPhone"));
        order.setOrdererEmail((String) orderData.get("ordererEmail"));
        order.setReceiverName((String) orderData.get("receiverName"));
        order.setReceiverPhone((String) orderData.get("receiverPhone"));
        order.setReceiverZip((String) orderData.get("receiverZip"));
        order.setReceiverAddr((String) orderData.get("receiverAddr"));
        order.setReceiverAddrDetail((String) orderData.get("receiverAddrDetail"));
        order.setDeliveryRequest((String) orderData.get("deliveryRequest"));
        order.setPaymentMethod((String) orderData.get("paymentMethod"));
        order.setTotalAmount((Integer) orderData.get("totalAmount"));
        order.setDiscountAmount((Integer) orderData.getOrDefault("discountAmount", 0));
        order.setDeliveryFee((Integer) orderData.getOrDefault("deliveryFee", 0));
        order.setFinalAmount((Integer) orderData.get("finalAmount"));

        // 주문 상태 설정 (결제 완료로 가정)
        order.setOrderStatus("PAID");  // 결제 기능 추가 시 PENDING -> PAID로 변경
        order.setPaymentStatus("PAID"); // 결제 기능 추가 시 PENDING -> PAID로 변경

        order.setAdultVerified((Boolean) orderData.getOrDefault("adultVerified", true));
        order.setAdultVerifiedAt(LocalDateTime.now());
        order.setPgProvider("payster"); // 결제 기능 추가

        return order;
    }

    private List<OrderItemDto> createOrderItemsFromCart(int orderCode, List<CartDto> cartItems) {
        List<OrderItemDto> orderItems = new ArrayList<>();

        for (CartDto cartItem : cartItems) {
            OrderItemDto orderItem = new OrderItemDto();
            orderItem.setOrderCode(orderCode);
            orderItem.setProductCode(cartItem.getProductCode());
            orderItem.setOptionCode(cartItem.getOptionCode());
            orderItem.setQuantity(cartItem.getQuantity());

            int unitPrice = cartItem.getPriceDto().getPriceSelling();
            int optionPrice = cartItem.getOptionCode() != null && cartItem.getProductOptionDto() != null
                    ? cartItem.getProductOptionDto().getOptionPrice() : 0;
            int totalPrice = (unitPrice + optionPrice) * cartItem.getQuantity();

            orderItem.setUnitPrice(unitPrice);
            orderItem.setOptionPrice(optionPrice);
            orderItem.setTotalPrice(totalPrice);

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    private List<OrderItemDto> createDirectOrderItems(int orderCode, int productCode, Integer optionCode,
                                                      int quantity, PriceDto priceDto, ProductOptionDto optionDto) {
        List<OrderItemDto> orderItems = new ArrayList<>();

        OrderItemDto orderItem = new OrderItemDto();
        orderItem.setOrderCode(orderCode);
        orderItem.setProductCode(productCode);
        orderItem.setOptionCode(optionCode);
        orderItem.setQuantity(quantity);

        int unitPrice = priceDto.getPriceSelling();
        int optionPrice = optionDto != null ? optionDto.getOptionPrice() : 0;
        int totalPrice = (unitPrice + optionPrice) * quantity;

        orderItem.setUnitPrice(unitPrice);
        orderItem.setOptionPrice(optionPrice);
        orderItem.setTotalPrice(totalPrice);

        orderItems.add(orderItem);
        return orderItems;
    }

    private void createInitialDelivery(int orderCode) {
        DeliveryDto delivery = new DeliveryDto();
        delivery.setOrderCode(orderCode);
        delivery.setDeliveryStatus("READY");
        orderDao.insertDelivery(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderDetail(String orderId) throws Exception {
        try {
            OrderDto order = orderDao.selectOrderByOrderId(orderId);
            if (order == null) {
                throw new Exception("주문을 찾을 수 없습니다: " + orderId);
            }
            return order;
        } catch (Exception e) {
            log.error("주문 상세 조회 실패: {}", orderId, e);
            throw new Exception("주문 조회에 실패했습니다", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getMemberOrders(int memCode, int page, int size) throws Exception {
        try {
            int offset = (page - 1) * size;
            List<OrderDto> orders = orderDao.selectOrdersByMemberCode(memCode, offset, size);

            for (OrderDto order : orders) {
                List<OrderItemDto> orderItems = orderDao.selectOrderItemsByOrderCode(order.getOrderCode());
                order.setOrderItems(orderItems);
            }

            return orders;
        } catch (Exception e) {
            log.error("회원 주문 목록 조회 실패: {}", memCode, e);
            throw new Exception("주문 목록 조회에 실패했습니다", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getGuestOrders(int guestCode, int page, int size) throws Exception {
        try {
            int offset = (page - 1) * size;
            List<OrderDto> orders = orderDao.selectOrdersByGuestCode(guestCode, offset, size);

            for (OrderDto order : orders) {
                List<OrderItemDto> orderItems = orderDao.selectOrderItemsByOrderCode(order.getOrderCode());
                order.setOrderItems(orderItems);
            }

            return orders;
        } catch (Exception e) {
            log.error("비회원 주문 목록 조회 실패: {}", guestCode, e);
            throw new Exception("주문 목록 조회에 실패했습니다", e);
        }
    }

    @Override
    public boolean updateOrderStatus(int orderCode, String orderStatus) throws Exception {
        try {
            int result = orderDao.updateOrderStatus(orderCode, orderStatus);
            return result > 0;
        } catch (Exception e) {
            log.error("주문 상태 변경 실패: {}, {}", orderCode, orderStatus, e);
            throw new Exception("주문 상태 변경에 실패했습니다", e);
        }
    }

    @Override
    public boolean updatePaymentStatus(int orderCode, String paymentStatus, String pgProvider, String pgTid) throws Exception {
        try {
            int result = orderDao.updatePaymentStatus(orderCode, paymentStatus, pgProvider, pgTid);
            return result > 0;
        } catch (Exception e) {
            log.error("결제 상태 변경 실패: {}, {}", orderCode, paymentStatus, e);
            throw new Exception("결제 상태 변경에 실패했습니다", e);
        }
    }

    @Override
    @Transactional
    public boolean cancelOrder(int orderCode, String orderId) throws Exception {
        try {
            log.info("주문 취소 시작: {}", orderId);

            boolean orderCancelled = updateOrderStatus(orderCode, "CANCELLED");
            if (!orderCancelled) {
                throw new Exception("주문 상태 변경에 실패했습니다.");
            }

            boolean paymentCancelled = updatePaymentStatus(orderCode, "CANCELLED", null, null);
            if (!paymentCancelled) {
                throw new Exception("결제 상태 변경에 실패했습니다.");
            }

            restoreInventory(orderCode);

            // 결제 기능 추가 시 PG사 취소 처리
            // cancelPaymentFromPG(orderId);

            log.info("주문 취소 완료: {}", orderId);
            return true;

        } catch (Exception e) {
            log.error("주문 취소 실패: {}", orderId, e);
            throw new Exception("주문 취소에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomSuffix = String.format("%03d", (int)(Math.random() * 1000));
        return "ORDER_" + timestamp + "_" + randomSuffix;
    }

    @Override
    @Transactional(readOnly = true)
    public int getMemberOrderCount(int memCode) {
        return orderDao.countOrdersByMemberCode(memCode);
    }

    @Override
    @Transactional(readOnly = true)
    public int getGuestOrderCount(int guestCode) {
        return orderDao.countOrdersByGuestCode(guestCode);
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderDetails(int orderCode) {
        return orderDao.getOrderDetails(orderCode);
    }

    @Override
    @Transactional
    public List<OrderDto> selectOrdersByMember(int memberCode) {
        return orderDao.selectOrdersByMember(memberCode);
    }

    @Override
    @Transactional
    public List<OrderDto> selectOrdersByGuest(int guestCode){
        return orderDao.selectOrdersByGuest(guestCode);
    }

    @Override
    @Transactional
    public List<OrderDto> selectOrdersByMemberForLimit(int memberCode){
        return orderDao.selectOrdersByMemberForLimit(memberCode);
    }
    @Override
    @Transactional
    public List<OrderDto> selectOrdersByGuestForLimit(int guestCode){
        return orderDao.selectOrdersByGuestForLimit(guestCode);
    }

    @Override
    @Transactional
    public int getFinalAmount(int code){
        return orderDao.getFinalAmount(code);
    }

    @Override
    @Transactional
    public DeliveryDto getTrackingNumber(int orderCode){
        return orderDao.getTrackingNumber(orderCode);
    }

    @Override
    public OrderItemDto getOrderItemDetail(int orderItemCode) {
        return orderDao.selectOrderItemDetail(orderItemCode);
    }
}