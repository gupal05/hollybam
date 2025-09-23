// src/main/java/com/hollybam/hollybam/services/OrderServiceImpl.java
package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_OrderDao;
import com.hollybam.hollybam.dao.IF_PaymentDao;
import com.hollybam.hollybam.dto.*;
import io.micrometer.common.lang.Nullable;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements IF_OrderService {
    @Autowired
    HttpSession session;
    @Autowired
    private IF_OrderDao orderDao;
    @Autowired
    private IF_PaymentDao paymentDao;
    @Autowired
    private ProductService productService;
    @Autowired
    private IF_PointService pointService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public OrderDto createOrderFromCart(Map<String, Object> orderData) throws Exception {
        try {
            log.info("장바구니 주문 생성 시작: {}", orderData);

            @SuppressWarnings("unchecked")
            List<Integer> cartCodes = (List<Integer>) orderData.get("cartCodes");
            List<CartDto> cartItems = paymentDao.selectCartItemsWithDetails(cartCodes);
            // 적립금 사용 정보 추출
            Integer usePoints = (Integer) orderData.get("usePoints");
            if (usePoints == null) usePoints = 0;

            if (cartItems.isEmpty()) {
                throw new Exception("주문할 상품이 없습니다.");
            }

            OrderDto order = createOrderFromData(orderData, cartItems);
            order.setDiscountAmount(order.getDiscountAmount() + usePoints);
            int memberCode = 0;
            if(session.getAttribute("member") != null){
                MemberDto member = (MemberDto) session.getAttribute("member");
                memberCode = member.getMemberCode();
            } else if (session.getAttribute("guest") != null) {
                GuestDto guest = (GuestDto) session.getAttribute("guest");
                memberCode = guest.getGuestCode();
            }
            order.setMemberCode(memberCode);
            orderDao.insertOrder(order);
            log.info("주문 저장 완료. 주문코드: {}", order.getOrderCode());

            List<OrderItemDto> orderItems = createOrderItemsFromCart(order.getOrderCode(), cartItems);
            orderDao.insertOrderItems(orderItems);
            if((boolean) orderData.get("payResult")){
                for(OrderItemDto orderItem : orderItems) {
                    orderDao.updateOrderCount(orderItem);
                }

                // 수정된 재고 차감 로직
                updateInventory(orderItems);

                orderDao.deleteCartItems(cartCodes);
                //createInitialDelivery(order.getOrderCode());

                // 쿠폰 처리 (기존 로직 유지)
                if(order.getMemCode() != null) {
                    if(orderData.get("couponCode") != null && !orderData.get("couponCode").toString().isEmpty()) {
                        int memCode = order.getMemCode();
                        int couponCode = Integer.parseInt(orderData.get("couponCode").toString());
                        int couponMemberCode = couponService.getCouponMemberCode(memCode, couponCode);
                        int discountAmount = Integer.parseInt(orderData.get("discountAmount").toString());
                        couponService.useCoupon(couponMemberCode, order.getOrderCode(), discountAmount);
                    }
                }

                // 적립금 처리 (회원인 경우만)
                if (order.getMemCode() != null && usePoints >= 0) {
                    processOrderPoints(
                            order.getOrderCode(),
                            order.getMemCode(),
                            usePoints,
                            (int)orderData.get("totalAmount")
                    );
                }

                // 🆕 할인코드 사용 내역 저장 (회원인 경우만)
                if(session.getAttribute("member") != null){
                    recordDiscountCodeUsageIfApplied(orderData, order.getMemCode(), order.getOrderCode());
                }
            }


            log.info("장바구니 주문 생성 완료: {}", order.getOrderId());
            session.setAttribute("orderData", orderData);
            session.setAttribute("order", order);
            session.setAttribute("orderItems", orderItems);
            return order;

        } catch (Exception e) {
            log.error("장바구니 주문 생성 실패", e);
            throw new Exception("주문 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> getCartProductName(int orderCode){
        return orderDao.getCartProductName(orderCode);
    }

    @Override
    @Transactional
    public OrderDto createDirectOrder(Map<String, Object> orderData) throws Exception {
        try {
            log.info("바로 구매 주문 생성 시작: {}", orderData);

            int productCode = (Integer) orderData.get("productCode");
            Integer optionCode = (Integer) orderData.get("optionCode");
            int quantity = (Integer) orderData.get("quantity");
            // 적립금 사용 정보 추출
            Integer usePoints = (Integer) orderData.get("usePoints");
            if (usePoints == null) usePoints = 0;

            PriceDto priceDto = paymentDao.selectProductPrice(productCode);
            ProductOptionDto optionDto = null;
            if (optionCode != null) {
                optionDto = paymentDao.selectProductOption(optionCode);
            }

            OrderDto order = createOrderFromData(orderData, null);
            order.setDiscountAmount(order.getDiscountAmount() + usePoints);
            int memberCode = 0;
            if(session.getAttribute("member") != null){
                MemberDto member = (MemberDto) session.getAttribute("member");
                memberCode = member.getMemberCode();
            } else if (session.getAttribute("guest") != null) {
                GuestDto guest = (GuestDto) session.getAttribute("guest");
                memberCode = guest.getGuestCode();
            }
            order.setMemberCode(memberCode);
            orderDao.insertOrder(order);
            log.info("바로 구매 주문 저장 완료. 주문코드: {}", order.getOrderCode());
            session.setAttribute("orderData", orderData);
            session.setAttribute("order", order);

            if((boolean) orderData.get("payResult")){
                // 쿠폰 처리 (기존 로직 유지)
                if(order.getMemCode() != null) {
                    if(orderData.get("couponCode") != null && !orderData.get("couponCode").toString().isEmpty()) {
                        int memCode = order.getMemCode();
                        int couponCode = Integer.parseInt(orderData.get("couponCode").toString());
                        int couponMemberCode = couponService.getCouponMemberCode(memCode, couponCode);
                        int discountAmount = Integer.parseInt(orderData.get("discountAmount").toString());
                        couponService.useCoupon(couponMemberCode, order.getOrderCode(), discountAmount);
                    }
                }

                // 적립금 처리 (회원인 경우만)
                if (order.getMemCode() != null && usePoints >= 0) {
                    processOrderPoints(
                            order.getOrderCode(),
                            order.getMemCode(),
                            usePoints,
                            (int)orderData.get("totalAmount")
                    );
                }

                List<OrderItemDto> orderItems = createDirectOrderItems(order.getOrderCode(), productCode, optionCode, quantity, priceDto, optionDto);
                orderDao.insertOrderItems(orderItems);
                session.setAttribute("orderItems", orderItems);
                for(OrderItemDto orderItem : orderItems) {
                    orderDao.updateOrderCount(orderItem);
                }

                // 수정된 재고 차감 로직
                updateInventory(orderItems);

                //createInitialDelivery(order.getOrderCode());
                // 🆕 할인코드 사용 내역 저장 (회원인 경우만)
                if(session.getAttribute("member") != null){
                    recordDiscountCodeUsageIfApplied(orderData, order.getMemCode(), order.getOrderCode());
                }
            }

            log.info("바로 구매 주문 생성 완료: {}", order.getOrderId());
            return order;

        } catch (Exception e) {
            log.error("바로 구매 주문 생성 실패", e);
            throw new Exception("주문 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }
    // 수정된 재고 차감 로직
    public void updateInventory(List<OrderItemDto> orderItems) throws Exception {
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

        if(session.getAttribute("member") != null){
            MemberDto member = (MemberDto) session.getAttribute("member");
            order.setMemCode(member.getMemberCode());
        }
        if(session.getAttribute("guest") != null){
            GuestDto guest = (GuestDto) session.getAttribute("guest");
            order.setGuestCode(guest.getGuestCode());
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
        order.setPaymentStatus("PENDING"); // 결제 기능 추가 시 PENDING -> PAID로 변경

        order.setAdultVerified((Boolean) orderData.getOrDefault("adultVerified", true));
        order.setAdultVerifiedAt(LocalDateTime.now());
        order.setDepositorName((String) orderData.get("depositorName"));

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

        // 🆕 특가 가격 확인 및 적용
        int unitPrice;
        if (productService.isSpecialSale(productCode) > 0) {
            // 특가 상품인 경우 특가 가격 사용
            unitPrice = productService.getProductDetailSalePrice(productCode);
        } else {
            // 일반 상품인 경우 판매가 사용
            unitPrice = priceDto.getPriceSelling();
        }

        int optionPrice = optionDto != null ? optionDto.getOptionPrice() : 0;
        int totalPrice = (unitPrice + optionPrice) * quantity;

        orderItem.setUnitPrice(unitPrice);
        orderItem.setOptionPrice(optionPrice);
        orderItem.setTotalPrice(totalPrice);

        orderItems.add(orderItem);
        return orderItems;
    }

    public void createInitialDelivery(int orderCode) {
        DeliveryDto delivery = new DeliveryDto();
        delivery.setOrderCode(orderCode);
        delivery.setDeliveryStatus("READY");
        orderDao.insertDelivery(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderDetail(String orderId) throws Exception {
        try {
            int memberCode = 0;
            if(session.getAttribute("member") != null){
                MemberDto member = (MemberDto) session.getAttribute("member");
                memberCode = member.getMemberCode();
            } else if (session.getAttribute("guest") != null) {
                GuestDto guest = (GuestDto) session.getAttribute("guest");
                memberCode = guest.getGuestCode();
            }
            String originId = orderId; // 예시
            String[] parts = originId.split("_");
            String processedOrderId;

            // 4개 조각일 경우 마지막 파트 제거
            if (parts.length == 4) {
                processedOrderId = String.join("_", parts[0], parts[1], parts[2]);
            } else {
                processedOrderId = originId;
            }
            OrderDto order = orderDao.selectOrderByOrderId(processedOrderId);
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
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int count = orderDao.getOrderCodeNumber(timestamp);
        return String.format("%s_%08d", timestamp, count + 1);
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

    @Override
    @Transactional
    public OrderDto createTempOrder(Map<String, Object> orderData) throws Exception {
        try {
            log.info("임시 주문 생성 시작: {}", orderData);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> orderItems = (List<Map<String, Object>>) orderData.get("orderItems");

            if (orderItems == null || orderItems.isEmpty()) {
                throw new Exception("주문할 상품이 없습니다.");
            }

            // 주문 기본 정보 생성
            OrderDto order = createOrderFromData(orderData, null);
            int memberCode = 0;
            if(session.getAttribute("member") != null){
                MemberDto member = (MemberDto) session.getAttribute("member");
                memberCode = member.getMemberCode();
            } else if (session.getAttribute("guest") != null) {
                GuestDto guest = (GuestDto) session.getAttribute("guest");
                memberCode = guest.getGuestCode();
            }
            order.setMemberCode(memberCode);
            orderDao.insertOrder(order);
            log.info("임시 주문 저장 완료. 주문코드: {}", order.getOrderCode());

            // 주문 아이템들 생성
            List<OrderItemDto> orderItemList = new ArrayList<>();

            for (Map<String, Object> itemData : orderItems) {
                Integer productCode = (Integer) itemData.get("productCode");
                Integer quantity = (Integer) itemData.get("quantity");
                Integer optionCode = (Integer) itemData.get("optionCode");

                // 상품 가격 정보 조회
                PriceDto priceDto = productService.getProductPrice(productCode);
                if (priceDto == null) {
                    throw new Exception("상품 가격 정보를 찾을 수 없습니다. productCode: " + productCode);
                }

                // 옵션 정보 조회 (있는 경우)
                ProductOptionDto optionDto = null;
                int optionPrice = 0;
                if (optionCode != null) {
                    optionDto = productService.getProductOption(optionCode);
                    if (optionDto != null) {
                        optionPrice = optionDto.getOptionPrice();
                    }
                }

                // 주문 아이템 생성
                OrderItemDto orderItem = new OrderItemDto();
                orderItem.setOrderCode(order.getOrderCode());
                orderItem.setProductCode(productCode);
                orderItem.setOptionCode(optionCode);
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(priceDto.getPriceSelling());
                orderItem.setOptionPrice(optionPrice);
                orderItem.setTotalPrice((priceDto.getPriceSelling() + optionPrice) * quantity);

                orderItemList.add(orderItem);
            }

            // 주문 아이템들 저장
            orderDao.insertOrderItems(orderItemList);

            // 주문 횟수 업데이트
            for (OrderItemDto orderItem : orderItemList) {
                orderDao.updateOrderCount(orderItem);
            }

            // 재고 차감
            updateInventory(orderItemList);

            // 배송 정보 생성
            createInitialDelivery(order.getOrderCode());

            log.info("임시 주문 생성 완료: {}", order.getOrderId());
            return order;

        } catch (Exception e) {
            log.error("임시 주문 생성 실패", e);
            throw new Exception("주문 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 주문 완료 후 적립금 처리 (사용 + 적립)
     * @param orderCode 주문 코드
     * @param memCode 회원 코드 (회원 주문인 경우만)
     * @param usePoints 사용한 적립금
     * @param finalAmount 최종 결제 금액
     */
    @Override
    @Transactional
    public void processOrderPoints(int orderCode, Integer memCode, int usePoints, int finalAmount) {
        System.out.println("확인 : "+usePoints+" & "+finalAmount);
        if (memCode == null) {
            return; // 비회원은 적립금 처리 불가
        }

        try {
            // 1. 적립금 사용 처리
            if (usePoints > 0) {
                boolean useResult = pointService.usePoints(
                        memCode,
                        usePoints,
                        orderCode,
                        "주문 결제 시 적립금 사용"
                );

                if (!useResult) {
                    throw new RuntimeException("적립금 사용 처리 실패");
                }
            }

            // 2. 구매 적립금 추가 (결제 금액의 1.5%)
            int rewardPoints = pointService.calculateRewardPoints(finalAmount);
            if (rewardPoints > 0) {
                boolean saveResult = pointService.savePoints(
                        memCode,
                        rewardPoints,
                        orderCode,
                        "구매 적립 (1.5%)"
                );

                if (!saveResult) {
                    log.warn("구매 적립금 지급 실패 - memCode: {}, orderCode: {}", memCode, orderCode);
                }
            }

            log.info("적립금 처리 완료 - 주문: {}, 사용: {}, 적립: {}", orderCode, usePoints, rewardPoints);

        } catch (Exception e) {
            log.error("적립금 처리 중 오류 발생 - orderCode: {}, memCode: {}", orderCode, memCode, e);
            throw new RuntimeException("적립금 처리 실패", e);
        }
    }

    /**
     * 주문 완료 시 할인코드 사용 내역 저장 (회원 전용)
     * @param orderData 주문 데이터
     * @param code 회원 코드 (회원인 경우에만 전달)
     */
    public void recordDiscountCodeUsageIfApplied(Map<String, Object> orderData, Integer code, Integer orderCode) {
        try {
            System.out.println("확인 : "+orderData);
            // 주문 데이터에서 할인코드 정보 추출
            String discountCodeId = (String) orderData.get("discountCode");

            // ===== 🆕 회원인 경우에만 할인코드 처리 =====
            if (session.getAttribute("member") != null) {
                if (discountCodeId != null && !discountCodeId.trim().isEmpty() && code != null) {
                    discountCodeId = discountCodeId.trim().toUpperCase();

                    // 할인코드 정보 조회
                    DiscountDto discountDto = discountService.getDiscountByCode(discountCodeId);

                    if (discountDto != null) {
                        // 회원 할인코드 사용 내역 저장 (중복 사용 허용)
                        discountService.recordDiscountCodeUsage(discountDto.getDiscountCode(), code, orderCode, (Integer)orderData.get("discountAmount"));
                        log.info("할인코드 사용 내역 저장 완료 (회원 전용): discountCode={} ({}), memberCode={}",
                                discountDto.getDiscountCode(), discountDto.getDiscountId(), code);
                    } else {
                        log.warn("할인코드 정보를 찾을 수 없음: discountCodeId={}", discountCodeId);
                    }
                }
            } else if (session.getAttribute("guest") != null) {
                // ===== 🆕 비회원이 할인코드를 사용하려고 하는 경우 경고 로그 =====
                if (discountCodeId != null && !discountCodeId.trim().isEmpty()) {
                    log.warn("비회원 할인코드 사용 시도 감지 및 차단: discountCodeId={}, guestCode={}",
                            discountCodeId, code);
                    // 비회원 할인코드 사용은 허용하지 않으므로 아무것도 하지 않음
                }
            }
        } catch (Exception e) {
            // 할인코드 사용 내역 저장 실패가 주문 전체를 실패시키지 않도록 함
            log.error("할인코드 사용 내역 저장 중 오류 발생 (주문은 정상 진행)", e);
        }
    }

    @Override
    @Transactional
    public String getProductName(int productCode){
        return orderDao.getProductName(productCode);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(String orderId, String status){
        orderDao.updatePaymentStatus(orderId, status);
    }

    @Override
    @Transactional
    public OrderDto createOrderByBank(Map<String, Object> orderData, HttpSession session) throws Exception {
        int usePoints = Integer.parseInt(String.valueOf(orderData.get("usePoints")));
        List<Integer> cartCodes = (List<Integer>) orderData.get("cartCodes");
        List<CartDto> cartItems = paymentDao.selectCartItemsWithDetails(cartCodes);
        OrderDto order = createOrderFromData(orderData, cartItems);
        order.setDiscountAmount(order.getDiscountAmount() + usePoints);
        int memberCode = 0;
        if(session.getAttribute("member") != null){
            MemberDto member = (MemberDto) session.getAttribute("member");
            memberCode = member.getMemberCode();
        } else if (session.getAttribute("guest") != null) {
            GuestDto guest = (GuestDto) session.getAttribute("guest");
            memberCode = guest.getGuestCode();
        }
        order.setMemberCode(memberCode);
        orderDao.insertOrder(order);
        List<OrderItemDto> orderItems = createOrderItemsFromCart(order.getOrderCode(), cartItems);
        orderDao.insertOrderItems(orderItems);

        int orderCode = order.getOrderCode();

        if(session.getAttribute("member") != null){
            MemberDto member =  (MemberDto) session.getAttribute("member");
            Integer points = Integer.valueOf(String.valueOf(orderData.get("usePoints")));
            int totalAmount = Integer.parseInt(String.valueOf(orderData.get("totalAmount")));
            String couponCodeStr = String.valueOf(orderData.get("couponCode"));
            String discountCodeStr = String.valueOf(orderData.get("discountCode"));
            if (couponCodeStr != null && !couponCodeStr.trim().isEmpty()) {
                int couponCode = Integer.parseInt(couponCodeStr);
                int discountAmount = Integer.parseInt(String.valueOf(orderData.get("discountAmount")));
                int couponMemberCode = couponService.getCouponMemberCode(member.getMemberCode(), couponCode);
                couponService.useCoupon(couponMemberCode, orderCode, discountAmount); //orderCode, discountAmount, couponMemberCode
                this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            } else if (discountCodeStr != null && !discountCodeStr.trim().isEmpty()) {
                DiscountDto discountDto = discountService.getDiscountByCode(discountCodeStr);
                Integer discountCode = discountDto.getDiscountCode();
                Integer discountAmount = Integer.valueOf(String.valueOf(orderData.get("discountAmount")));
                // ✅ 할인코드 + 포인트 사용
                discountService.recordDiscountCodeUsage(discountCode, member.getMemberCode(), orderCode, discountAmount);
                this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            } else if (points != null && points > 0) {
                // ✅ 포인트만 사용
                this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            } else {
                // ❗ 아무 것도 안 쓴 경우
                this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            }
        }

        updateInventory(orderItems);
        orderDao.deleteCartItems(cartCodes);
        for(OrderItemDto orderItem : orderItems) {
                orderDao.updateOrderCount(orderItem);
            }

        return order;
    }

    @Override
    @Transactional
    public OrderDto createDirectOrderByTrans(Map<String, Object> orderData, HttpSession session) throws Exception {
        int usePoints = Integer.parseInt(String.valueOf(orderData.get("usePoints")));
        OrderDto order = createOrderFromData(orderData, null);
        order.setDiscountAmount(order.getDiscountAmount() + usePoints);
        int memberCode = 0;
        if(session.getAttribute("member") != null){
            MemberDto member = (MemberDto) session.getAttribute("member");
            memberCode = member.getMemberCode();
        } else if (session.getAttribute("guest") != null) {
            GuestDto guest = (GuestDto) session.getAttribute("guest");
            memberCode = guest.getGuestCode();
        }
        order.setMemberCode(memberCode);
        orderDao.insertOrder(order);
        int productCode = (Integer) orderData.get("productCode");
        Integer optionCode = (Integer) orderData.get("optionCode");
        int quantity = (Integer) orderData.get("quantity");
        PriceDto priceDto = paymentDao.selectProductPrice(productCode);
        ProductOptionDto optionDto = null;
        if (optionCode != null) {
            optionDto = paymentDao.selectProductOption(optionCode);
        }
        List<OrderItemDto> orderItems = createDirectOrderItems(order.getOrderCode(), productCode, optionCode, quantity, priceDto, optionDto);
        orderDao.insertOrderItems(orderItems);

        int orderCode = order.getOrderCode();

        if(session.getAttribute("member") != null){
            MemberDto member =  (MemberDto) session.getAttribute("member");
            Integer points = Integer.valueOf(String.valueOf(orderData.get("usePoints")));
            int totalAmount = Integer.parseInt(String.valueOf(orderData.get("totalAmount")));
            String couponCodeStr = String.valueOf(orderData.get("couponCode"));
            String discountCodeStr = String.valueOf(orderData.get("discountCode"));
            if (couponCodeStr != null && !couponCodeStr.trim().isEmpty()) {
                int couponCode = Integer.parseInt(couponCodeStr);
                int discountAmount = Integer.parseInt(String.valueOf(orderData.get("discountAmount")));
                int couponMemberCode = couponService.getCouponMemberCode(member.getMemberCode(), couponCode);
                couponService.useCoupon(couponMemberCode, orderCode, discountAmount); //orderCode, discountAmount, couponMemberCode
                this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            } else if (discountCodeStr != null && !discountCodeStr.trim().isEmpty()) {
                DiscountDto discountDto = discountService.getDiscountByCode(discountCodeStr);
                Integer discountCode = discountDto.getDiscountCode();
                Integer discountAmount = Integer.valueOf(String.valueOf(orderData.get("discountAmount")));
                    // ✅ 할인코드 + 포인트 사용
                    discountService.recordDiscountCodeUsage(discountCode, member.getMemberCode(), orderCode, discountAmount);
                    this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            } else if (points != null && points > 0) {
                // ✅ 포인트만 사용
                this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            } else {
                // ❗ 아무 것도 안 쓴 경우
                this.processOrderPoints(orderCode, member.getMemberCode(), usePoints, totalAmount);
            }
        }

        updateInventory(orderItems);
        for(OrderItemDto orderItem : orderItems) {
                orderDao.updateOrderCount(orderItem);
            }

        return order;
    }

    // ============================================
// OrderServiceImpl 구현 (최적화된 버전)
// ============================================

    @Override
    @Transactional
    public boolean instantDeleteOrder(String orderId, String reason) throws Exception {
        try {
            log.info("⚡ 즉시 주문 삭제 시작: {}", orderId);

            // 1. 주문 조회 (기존 메서드 활용)
            OrderDto order = getOrderDetail(orderId);
            if (order == null) {
                log.warn("⚠️ 삭제할 주문을 찾을 수 없음: {}", orderId);
                return true; // 없으면 성공으로 처리
            }

            int orderCode = order.getOrderCode();

            // 2. 주문 아이템 조회 (기존 메서드 활용)
            List<Map<String, Object>> orderItems = getOrderDetails(orderCode);

            // 3. 🔄 재고 복원 (기존 메서드들 활용)
            for (Map<String, Object> item : orderItems) {
                try {
                    Integer productCode = (Integer) item.get("productCode");
                    Integer quantity = (Integer) item.get("quantity");
                    Integer optionCode = (Integer) item.get("optionCode");

                    if (productCode == null || quantity == null) continue;

                    if (optionCode != null) {
                        // 기존 재고 복원 메서드 활용
                        orderDao.restoreOptionQuantity(optionCode, quantity);
                        orderDao.updateProductTotalQuantityFromOptions(productCode);
                    } else {
                        // 기존 재고 복원 메서드 활용
                        orderDao.restoreProductQuantity(productCode, quantity);
                    }

                } catch (Exception e) {
                    log.warn("⚠️ 개별 재고 복원 실패 (계속 진행)", e);
                }
            }

            // 4. 🗑️ 직접 SQL로 즉시 삭제 (MyBatis 대신)
            try {
                // 주문 아이템 삭제 - 직접 SQL 실행
                jdbcTemplate.update("DELETE FROM order_items WHERE order_code = ?", orderCode);
                log.info("✅ order_items 삭제 완료");

                // 주문 삭제 - 직접 SQL 실행
                int deletedOrder = jdbcTemplate.update("DELETE FROM orders WHERE order_code = ?", orderCode);
                log.info("✅ orders 삭제 완료: {}건", deletedOrder);

            } catch (Exception e) {
                log.error("❌ 직접 SQL 삭제 실패", e);

                // 테이블명이 다를 수 있으니 order_item도 시도
                try {
                    jdbcTemplate.update("DELETE FROM order_items WHERE order_code = ?", orderCode);
                    jdbcTemplate.update("DELETE FROM orders WHERE order_code = ?", orderCode);
                    log.info("✅ order_item 테이블명으로 삭제 성공");
                } catch (Exception e2) {
                    log.error("❌ order_item 테이블명으로도 삭제 실패", e2);
                    return false;
                }
            }

            log.info("⚡ 즉시 삭제 완료: {} ({})", orderId, reason);
            return true;

        } catch (Exception e) {
            log.error("❌ 즉시 주문 삭제 실패: {}", orderId, e);
            return false;
        }
    }

    /**
     * 🔄 빠른 재고 복원 (최적화된 버전)
     */
    private void restoreInventoryFast(List<Map<String, Object>> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        log.info("🔄 재고 빠른 복원 시작 - {}개 아이템", orderItems.size());

        for (Map<String, Object> item : orderItems) {
            try {
                Integer productCode = (Integer) item.get("productCode");
                Integer quantity = (Integer) item.get("quantity");
                Integer optionCode = (Integer) item.get("optionCode");

                if (productCode == null || quantity == null) continue;

                if (optionCode != null) {
                    // 옵션 재고 복원
                    orderDao.restoreOptionQuantityFast(optionCode, quantity);
                    orderDao.updateProductTotalQuantityFromOptions(productCode);
                } else {
                    // 상품 재고 복원
                    orderDao.restoreProductQuantityFast(productCode, quantity);
                }

            } catch (Exception e) {
                log.warn("⚠️ 개별 재고 복원 실패 (계속 진행)", e);
                // 개별 실패는 무시하고 계속 진행
            }
        }

        log.info("🔄 재고 빠른 복원 완료");
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getOrderItemsList(int orderCode){
        return orderDao.getOrderItemsList(orderCode);
    }

    private static final int FREE_SHIPPING_THRESHOLD = 50_000;
    private static final int SHIPPING_FEE = 3_000;

    private static final int FREE_DELIVERY_THRESHOLD = 50000;
    private static final int DELIVERY_FEE = 3000;

    @Override
    @Transactional
    public Map<String, Object> applyRefundRequest(Map<String, Object> refundOrder,
                                                  List<Map<String, Object>> products,
                                                  MemberDto member) {

        final int orderCode = Integer.parseInt(refundOrder.get("orderCode").toString());

        // 중복 신청 방지 체크
        if (orderDao.countRefundBatchesByOrder(orderCode) > 0) {
            throw new IllegalStateException("이미 환불/취소 신청된 주문입니다.");
        }

        final String actionRaw = String.valueOf(refundOrder.getOrDefault("actionType", "")).trim();
        final String action = actionRaw.isEmpty() ? "CANCEL" : actionRaw.toUpperCase();
        final boolean isCancel = "CANCEL".equals(action);
        final boolean isReturn = "RETURN".equals(action);

        final String reasonRaw = String.valueOf(refundOrder.getOrDefault("cancelReason", "")).trim();
        final boolean isDefect = "상품불량".equals(reasonRaw);

        System.out.println("========== [환불 신청 처리 시작] ==========");
        System.out.println("주문코드: " + orderCode + ", 액션: " + action + ", 사유: " + reasonRaw);

        // 주문 금액 정보 조회
        Map<String, Object> amounts = orderDao.getOrderAmounts(orderCode);
        if (amounts == null) throw new IllegalArgumentException("order not found: " + orderCode);

        final int P0 = ((Number) amounts.get("itemsSubtotal")).intValue(); // 할인 전 총 상품금액
        final int S0 = ((Number) amounts.get("deliveryFee")).intValue();   // 원래 배송비
        final int F0 = ((Number) amounts.get("finalAmount")).intValue();   // 실제 결제액
        final int D0 = P0 + S0 - F0; // 전체 할인액 (상품할인 + 배송비할인)

        final Integer memCodeFromOrder = (Integer) amounts.get("memCode");
        final boolean isMemberOrder = (memCodeFromOrder != null);

        System.out.println("주문 정보 - P0(상품합): " + P0 + ", S0(배송비): " + S0 +
                ", F0(결제액): " + F0 + ", D0(전체할인): " + D0);

        // 환불 배치 생성
        Map<String, Object> batch = new HashMap<>();
        batch.put("orderCode", orderCode);
        batch.put("refundAmount", 0); // 계산 후 업데이트
        batch.put("refundDeliveryFee", 0); // 계산 후 업데이트
        batch.put("cancelReason", reasonRaw);
        batch.put("actionType", action);
        orderDao.insertRefundBatch(batch);
        int refundBatchId = ((Number) batch.get("refundBatchId")).intValue();

        // 환불 대상 상품 라인 계산 & refund_items 기록
        int Pr = 0; // 환불 대상 상품합 (할인 전)
        for (Map<String, Object> p : products) {
            int productCode = orderDao.getProductCodeByProductId(p.get("productId").toString());
            int orderItemCode = orderDao.getOrderItemsCodeByProductCode(orderCode, productCode);

            Map<String, Object> row = orderDao.getOrderItemPrice(orderItemCode);
            if (row == null) throw new IllegalArgumentException("order item not found: " + orderItemCode);

            int unitPrice = ((Number) row.get("unitPrice")).intValue();
            int optionPrice = ((Number) row.get("optionPrice")).intValue();
            int qtyOriginal = ((Number) row.get("quantity")).intValue();
            int qtyRefund = Integer.parseInt(p.get("selectedQuantity").toString());

            if (qtyRefund <= 0 || qtyRefund > qtyOriginal) {
                throw new IllegalArgumentException("invalid refund qty for item: " + orderItemCode);
            }

            int lineRefund = (unitPrice + optionPrice) * qtyRefund;
            Pr += lineRefund;

            Map<String, Object> one = new HashMap<>();
            one.put("refundBatchId", refundBatchId);
            one.put("orderItemsCode", orderItemCode);
            one.put("refundQuantity", qtyRefund);
            one.put("refundAmount", lineRefund);
            orderDao.insertRefundItem(one);
        }

        System.out.println("환불 대상 상품합 (할인 전): " + Pr);

        // 전체 환불 여부 판단
        boolean fullQty = true;
        int orderItemsCount = orderDao.countOrderItems(orderCode);
        if (products.size() != orderItemsCount) {
            fullQty = false;
        } else {
            for (Map<String, Object> p : products) {
                int productCode = orderDao.getProductCodeByProductId(p.get("productId").toString());
                int oiCode = orderDao.getOrderItemsCodeByProductCode(orderCode, productCode);
                Map<String, Object> row = orderDao.getOrderItemPrice(oiCode);
                int originalQty = ((Number) row.get("quantity")).intValue();
                int selectedQty = Integer.parseInt(p.get("selectedQuantity").toString());
                if (originalQty != selectedQty) {
                    fullQty = false;
                    break;
                }
            }
        }

        int finalRefundAmount; // 최종 환불액
        int returnShippingDeduct = 0; // 반품 배송비 차감

        if (fullQty) {
            // ===== 전체 환불 =====
            System.out.println("=== 전체 환불 처리 ===");

            // 쿠폰/할인코드 복원
            if (isMemberOrder) {
                if (orderDao.isCouponUsedOrder(orderCode) > 0) {
                    int cmc = orderDao.getCouponMemberCode(orderCode);
                    orderDao.updateCouponMemberByRefund(Map.of("couponMemberCode", cmc));
                    System.out.println("쿠폰 복원 완료");
                }
                if (orderDao.isDiscountCodeUsedOrder(orderCode) > 0) {
                    orderDao.delDiscountCodeUsageByRefund(orderCode);
                    System.out.println("할인코드 복원 완료");
                }
            }

            if (isCancel) {
                finalRefundAmount = F0; // 전액 환불
            } else {
                returnShippingDeduct = (isDefect) ? 0 : 3000;
                finalRefundAmount = F0 - returnShippingDeduct;
            }

        } else {
            // ===== 부분 환불 (🆕 개선된 계산) =====
            System.out.println("=== 부분 환불 처리 (개선) ===");

            // 1. 환불 상품의 할인 배분 계산
            double refundRatio = (double) Pr / P0;
            int allocatedDiscount = (int) Math.round(D0 * refundRatio);

            System.out.println("환불 비율: " + String.format("%.4f", refundRatio));
            System.out.println("배분된 할인액: " + allocatedDiscount);

            // 2. 환불 상품의 실제 결제액
            int refundItemsPaymentAmount = Pr - allocatedDiscount;

            // 3. 남은 주문 계산
            int P1 = P0 - Pr; // 남은 상품 할인 전 금액
            int D1 = D0 - allocatedDiscount; // 남은 할인액
            int remainingPaymentAmount = P1 - D1; // 남은 상품 실제 결제액

            System.out.println("남은 상품 할인 전: " + P1 + ", 남은 할인: " + D1 +
                    ", 남은 실제 결제액: " + remainingPaymentAmount);

            // 4. 배송비 재계산
            int S1 = (remainingPaymentAmount >= 50000) ? 0 : 3000;
            int deliveryFeeDifference = S0 - S1;

            System.out.println("원래 배송비: " + S0 + ", 새 배송비: " + S1 +
                    ", 배송비 차이: " + deliveryFeeDifference);

            // 5. 쿠폰 회수 계산
            int couponRollback = 0;
            if (remainingPaymentAmount < 10000) { // 최소주문금액 미달
                couponRollback = allocatedDiscount;
                System.out.println("쿠폰 회수 (최소주문금액 미달): " + couponRollback);
            }

            // 6. 반품 배송비 차감
            if (isReturn && !isDefect) {
                returnShippingDeduct = 3000;
            }

            // 7. 최종 환불액 계산
            finalRefundAmount = refundItemsPaymentAmount + deliveryFeeDifference + couponRollback - returnShippingDeduct;
            finalRefundAmount = Math.max(0, finalRefundAmount);

            System.out.println("=== 최종 환불액 계산 ===");
            System.out.println("환불상품 결제액: " + refundItemsPaymentAmount);
            System.out.println("+ 배송비 차이: " + deliveryFeeDifference);
            System.out.println("+ 쿠폰 회수: " + couponRollback);
            System.out.println("- 반품비 차감: " + returnShippingDeduct);
            System.out.println("= 최종 환불액: " + finalRefundAmount);
        }

        // refund_batches 최종 업데이트
        orderDao.updateRefundBatchTotals(Map.of(
                "refundBatchId", refundBatchId,
                "refundAmount", finalRefundAmount,
                "refundDeliveryFee", returnShippingDeduct
        ));

        // 적립금 재계산 (회원만)
        if (isMemberOrder && orderDao.isOrderPoint(orderCode) > 0) {
            List<Map<String, Object>> prev = orderDao.getPointInfo(orderCode);
            if (prev != null && !prev.isEmpty()) {
                orderDao.deletePointsByCodes(prev); // 기존 SAVE 삭제
            }

            // 남은 결제금액 계산
            int remainingFinalAmount;
            if (fullQty) {
                remainingFinalAmount = 0;
            } else {
                int P1 = P0 - Pr;
                int D1 = D0 - (int) Math.round(D0 * ((double) Pr / P0));
                int S1 = ((P1 - D1) >= 50000) ? 0 : 3000;
                remainingFinalAmount = P1 + S1 - D1;
            }

            int afterPoint = (int) Math.floor(remainingFinalAmount * 0.015);
            if (afterPoint > 0) {
                PointDto pointDto = new PointDto();
                int memberCode = (member != null) ? member.getMemberCode() : memCodeFromOrder;
                pointDto.setMemberCode(memberCode);
                pointDto.setPointChange(afterPoint);
                pointDto.setPointType("SAVE");
                pointDto.setDescription("구매 금액 1.5% 적립(환불 재계산)");
                pointDto.setRelatedOrderCode(orderCode);
                orderDao.insertPoint(pointDto);
            }

            System.out.println("적립금 재계산 완료 - 남은금액: " + remainingFinalAmount + ", 적립: " + afterPoint);
        }

        // 주문 상태 업데이트
        String newOrderStatus = isCancel ? "REFUND_PENDING" : "RETURN_REQUESTED";
        String newPaymentStatus = "PAID";
        String newSimpleStatus = "CANCELLED";
        orderDao.updateOrderStatusForRefund(orderCode, newOrderStatus, newPaymentStatus, newSimpleStatus);

        Map<String, Object> out = new HashMap<>();
        out.put("refundBatchId", refundBatchId);
        out.put("remainingAmount", fullQty ? 0 : (P0 - Pr));
        out.put("refundAmount", finalRefundAmount);
        out.put("fullRefund", fullQty);
        out.put("type", action);
        out.put("defectReason", isDefect);
        out.put("newOrderStatus", newOrderStatus);
        out.put("newPaymentStatus", newPaymentStatus);
        out.put("newSimpleStatus", newSimpleStatus);

        System.out.println("========== [환불 신청 처리 완료] ==========");
        System.out.println("최종 환불액: " + finalRefundAmount);
        System.out.println("배치 ID: " + refundBatchId);
        System.out.println("=========================================");

        return out;
    }

    @Override
    public List<Map<String, Object>> getOrderItemsForRefund(int orderCode) {
        return orderDao.getOrderItemsForRefund(orderCode);
    }
    @Override
    public Map<String, Object> getOrderHeaderForRefund(int orderCode) {
        return orderDao.getOrderHeaderForRefund(orderCode);
    }

    @Override
    public Map<String, Object> computeRefundQuote(Map<String, Object> reqData) {
        System.out.println("========== [환불 견적 계산 시작] ==========");

        // 1) 입력 검증
        if (reqData == null || reqData.isEmpty()) {
            throw new IllegalArgumentException("환불 요청 데이터가 null입니다.");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) reqData.get("products");
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("환불 대상 상품이 없습니다.");
        }

        // 2) 기본 데이터 추출
        int orderCode = toInt(reqData.get("orderCode"));
        String actionType = reqData.get("actionType").toString().toUpperCase();
        String cancelReason = reqData.get("cancelReason").toString();
        boolean isCancel = "CANCEL".equals(actionType);
        boolean isReturn = "RETURN".equals(actionType);
        boolean isDefect = "상품불량".equals(cancelReason);

        System.out.println("주문코드: " + orderCode + ", 액션: " + actionType + ", 사유: " + cancelReason);

        // 3) 주문 헤더 정보 조회
        Map<String, Object> orderHeader = orderDao.getOrderHeaderForRefund(orderCode);
        if (orderHeader == null) {
            throw new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderCode);
        }

        int totalAmount = toInt(orderHeader.get("totalAmount"));     // 할인 전 총 상품금액
        int discountAmount = toInt(orderHeader.get("discountAmount")); // 전체 할인액
        int deliveryFee = toInt(orderHeader.get("deliveryFee"));     // 원래 배송비
        int finalAmount = toInt(orderHeader.get("finalAmount"));     // 실제 결제액

        System.out.println("주문 정보 - 총상품금액: " + totalAmount + ", 할인액: " + discountAmount +
                ", 배송비: " + deliveryFee + ", 결제액: " + finalAmount);

        // 4) 환불 대상 상품들의 상세 정보 및 금액 계산
        int selectedTotal = 0; // 환불할 상품들의 할인 전 금액 합계
        List<Map<String, Object>> productDetails = new ArrayList<>();

        for (Map<String, Object> product : products) {
            int orderItemCode = toInt(product.get("orderItemCode"));
            int selectedQuantity = toInt(product.get("selectedQuantity"));

            System.out.println("=== 상품 " + productDetails.size() + 1 + " 처리 ===");
            System.out.println("orderItemCode: " + orderItemCode);
            System.out.println("selectedQuantity: " + selectedQuantity + " (요청에서 파싱됨)");

            // 해당 주문 아이템의 상세 정보 조회
            Map<String, Object> itemDetail = orderDao.getOrderItemByCode(orderItemCode);
            if (itemDetail == null) {
                throw new IllegalArgumentException("주문 상품을 찾을 수 없습니다: " + orderItemCode);
            }

            System.out.println("DB 조회 결과: " + itemDetail);

            int unitPrice = toInt(itemDetail.get("unitPrice"));
            int orderedQuantity = toInt(itemDetail.get("orderedQuantity"));

            if (selectedQuantity > orderedQuantity) {
                throw new IllegalArgumentException("환불 수량이 주문 수량을 초과합니다.");
            }

            int itemSubtotal = unitPrice * selectedQuantity; // 할인 전 금액
            selectedTotal += itemSubtotal;

            productDetails.add(Map.of(
                    "orderItemCode", orderItemCode,
                    "selectedQuantity", selectedQuantity,
                    "unitPrice", unitPrice,
                    "lineRefundAmount", itemSubtotal
            ));

            System.out.println("라인 계산 완료 - unitPrice: " + unitPrice +
                    ", selectedQuantity: " + selectedQuantity +
                    ", lineTotal: " + itemSubtotal +
                    ", selectedTotal: " + selectedTotal);
        }

        System.out.println("전체 선택 상품 금액: " + selectedTotal);

        // 5) 전체/부분 환불 여부 판단
        int totalOrderItems = orderDao.countOrderItems(orderCode);
        boolean isFullRefund = (products.size() == totalOrderItems);

        if (isFullRefund) {
            // 모든 아이템의 수량도 확인
            for (Map<String, Object> product : products) {
                int orderItemCode = toInt(product.get("orderItemCode"));
                Map<String, Object> line = orderDao.getOrderItemByCode(orderItemCode);
                int orderedQuantity = toInt(line.get("orderedQuantity"));
                int selectedQuantity = toInt(product.get("selectedQuantity"));

                if (selectedQuantity != orderedQuantity) {
                    isFullRefund = false;
                    System.out.println("부분 환불로 판정 - 수량 불일치");
                    break;
                }
            }
        }

        System.out.println("환불 유형: " + (isFullRefund ? "전체환불" : "부분환불"));

        // 6) 환불 금액 계산
        int refundAmount = 0;
        int deliveryFeeDeduction = 0;
        int discountRollback = 0;

        if (isFullRefund) {
            // ===== 전체 환불 =====
            System.out.println("=== 전체 환불 계산 ===");

            if (isCancel) {
                refundAmount = finalAmount; // 전액 환불
                System.out.println("전체 취소 - 전액 환불: " + refundAmount);
            } else {
                deliveryFeeDeduction = (isDefect) ? 0 : 3000;
                refundAmount = finalAmount - deliveryFeeDeduction;
                System.out.println("전체 반품 - 환불액: " + refundAmount + ", 배송비 차감: " + deliveryFeeDeduction);
            }

        } else {
            // ===== 부분 환불 (🆕 할인 비례배분 적용) =====
            System.out.println("=== 부분 환불 계산 (할인 비례배분) ===");

            // 1. 환불 상품의 할인 배분 계산
            double refundRatio = (double) selectedTotal / totalAmount;
            int allocatedDiscount = (int) Math.round(discountAmount * refundRatio);

            System.out.println("환불 비율: " + String.format("%.4f", refundRatio) +
                    " (" + selectedTotal + " / " + totalAmount + ")");
            System.out.println("환불 상품에 배분된 할인액: " + allocatedDiscount);

            // 2. 환불 상품의 실제 결제액 계산
            int refundItemsPaymentAmount = selectedTotal - allocatedDiscount;
            System.out.println("환불 상품의 실제 결제액: " + refundItemsPaymentAmount);

            // 3. 남은 주문 계산
            int remainingItemsSubtotal = totalAmount - selectedTotal; // 남은 상품 할인 전 금액
            int remainingDiscount = discountAmount - allocatedDiscount; // 남은 할인액
            int remainingPaymentAmount = remainingItemsSubtotal - remainingDiscount; // 남은 상품 실제 결제액

            System.out.println("남은 상품 할인 전: " + remainingItemsSubtotal);
            System.out.println("남은 할인액: " + remainingDiscount);
            System.out.println("남은 상품 실제 결제액: " + remainingPaymentAmount);

            // 4. 남은 주문의 배송비 재계산
            int newDeliveryFee = (remainingPaymentAmount >= 50000) ? 0 : 3000;
            int deliveryFeeDifference = deliveryFee - newDeliveryFee;

            System.out.println("원래 배송비: " + deliveryFee + ", 새 배송비: " + newDeliveryFee +
                    ", 배송비 차이: " + deliveryFeeDifference);

            // 5. 쿠폰 회수 계산 (남은 실제 결제액 기준)
            boolean shouldRollbackDiscount = false;
            if (remainingPaymentAmount < 10000) { // 쿠폰 최소주문금액 확인
                shouldRollbackDiscount = true;
                discountRollback = allocatedDiscount; // 환불 상품에 배분된 할인액만 회수
            }

            System.out.println("할인 회수 여부: " + shouldRollbackDiscount + " (남은결제액 " +
                    remainingPaymentAmount + " < 10000)");
            System.out.println("할인 회수액: " + discountRollback);

            // 6. 반품 배송비 차감 계산
            if (isReturn && !isDefect) {
                deliveryFeeDeduction = 3000; // 변심 반품시 고객 부담
                System.out.println("반품 배송비 차감: " + deliveryFeeDeduction);
            } else if (isCancel) {
                // 취소시 배송비 차이 처리 (무료배송 조건 상실)
                if (deliveryFeeDifference < 0) { // 새로 배송비가 부과되는 경우
                    deliveryFeeDeduction = Math.abs(deliveryFeeDifference); // 3000원 차감
                    deliveryFeeDifference = 0; // 차이는 이미 차감에 반영됨
                    System.out.println("취소로 인한 배송비 부과 - 고객 부담: " + deliveryFeeDeduction);
                }
            }

            // 7. 최종 환불액 계산
            refundAmount = refundItemsPaymentAmount + deliveryFeeDifference + discountRollback - deliveryFeeDeduction;
            refundAmount = Math.max(0, refundAmount); // 음수 방지

            System.out.println("=== 최종 환불액 상세 계산 ===");
            System.out.println("환불상품 실제 결제액: " + refundItemsPaymentAmount);
            System.out.println("+ 배송비 차이 환불: " + deliveryFeeDifference);
            System.out.println("+ 할인 회수액: " + discountRollback);
            System.out.println("- 배송비/반품비 차감: " + deliveryFeeDeduction);
            System.out.println("= 최종 환불액: " + refundAmount);
        }

        // 7) 응답 생성
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("refundAmount", refundAmount);
        result.put("deliveryFeeDeduction", deliveryFeeDeduction);
        result.put("discountRollback", discountRollback);
        result.put("isFullRefund", isFullRefund);
        result.put("selectedTotal", selectedTotal);
        result.put("products", productDetails);

        // 상세 계산 정보 추가 (디버깅용)
        if (!isFullRefund) {
            result.put("allocatedDiscount", (int) Math.round(discountAmount * ((double) selectedTotal / totalAmount)));
            result.put("remainingItemsSubtotal", totalAmount - selectedTotal);
            result.put("remainingPaymentAmount", (totalAmount - selectedTotal) - (discountAmount - (int) Math.round(discountAmount * ((double) selectedTotal / totalAmount))));
            result.put("deliveryFeeDifference", deliveryFee - (((totalAmount - selectedTotal - (discountAmount - (int) Math.round(discountAmount * ((double) selectedTotal / totalAmount)))) >= 50000) ? 0 : 3000));
        }

        System.out.println("========== [환불 견적 계산 완료] ==========");
        System.out.println("최종 환불 금액: " + refundAmount);
        System.out.println("배송비 차감: " + deliveryFeeDeduction);
        System.out.println("할인 회수: " + discountRollback);
        System.out.println("=========================================");

        return result;
    }

    /**
     * 안전한 정수 변환 유틸리티
     */
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int isBuyCoupon(){
        return orderDao.isBuyCoupon();
    }

    @Override
    @Transactional
    public void insBuyCoupon(int memberCode, int couponCode){
        orderDao.insBuyCoupon(memberCode, couponCode);
    }

    private static int safe(Integer v) { return v == null ? 0 : v; }
}