package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.*;
import java.util.List;

public interface IF_PaymentService {
    /**
     * 장바구니에서 주문 정보 계산
     */
    PaymentRequestDto calculateOrderFromCart(List<Integer> cartCodes, Integer memberCode, Integer guestCode);

    /**
     * 장바구니 상품 상세 정보 조회 (상품명, 이미지, 옵션 등 포함)
     */
    List<CartDto> getCartItemsWithDetails(List<Integer> cartCodes);

    /**
     * 바로 구매시 주문 정보 계산
     */
    PaymentRequestDto calculateDirectPurchase(int productCode, Integer optionCode, int quantity,
                                              Integer memberCode, Integer guestCode);

    void insertPaymentLog(PaymentLogDto paymentLogDto);
}