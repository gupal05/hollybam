package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface IF_PaymentDao {

    // 주문 관련
    int insertOrder(OrderDto orderDto);
    int insertOrderItems(@Param("orderItems") List<OrderItemDto> orderItems);
    OrderDto selectOrderByOrderId(String orderId);
    List<OrderDto> selectOrdersByMember(int memCode);
    List<OrderDto> selectOrdersByGuest(int guestCode);
    int updateOrderStatus(@Param("orderId") String orderId, @Param("status") String status);
    int updatePaymentInfo(@Param("orderId") String orderId,
                          @Param("impUid") String impUid,
                          @Param("pgProvider") String pgProvider,
                          @Param("pgTid") String pgTid,
                          @Param("paymentStatus") String paymentStatus);

    // 장바구니 관련
    List<CartDto> selectCartItemsWithDetails(@Param("cartCodes") List<Integer> cartCodes);
    int deleteCartItems(@Param("cartCodes") List<Integer> cartCodes);

    // 재고 관련
    int updateProductStock(@Param("productCode") int productCode, @Param("quantity") int quantity);
    int updateOptionStock(@Param("optionCode") int optionCode, @Param("quantity") int quantity);
    int selectProductStock(int productCode);
    int selectOptionStock(int optionCode);

    // 성인인증 관련
    boolean isAdultVerifiedMember(int memCode);
    boolean isAdultVerifiedGuest(int guestCode);
    int selectGuestCodeByUuid(String guestUuid);

    // 결제 로그
    int insertPaymentLog(PaymentLogDto paymentLogDto);

    // 주문 아이템 조회
    List<OrderItemDto> selectOrderItemsByOrderCode(int orderCode);

    List<CartDto> getCartItemsWithDetails(List<Integer> cartCodes);
}