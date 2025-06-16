package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.*;
import java.util.List;

public interface IF_PaymentService {

    /**
     * 결제 준비 - 주문 정보 검증 및 임시 주문 생성
     */
    String preparePayment(PaymentRequestDto paymentRequestDto);

    /**
     * 결제 완료 처리 - PG사 결제 검증 및 주문 확정
     */
    PaymentResponseDto completePayment(String impUid, String orderId);

    /**
     * 결제 실패 처리
     */
    void failPayment(String orderId, String errorMsg);

    /**
     * 주문 조회
     */
    OrderDto getOrder(String orderId);

    /**
     * 주문 목록 조회 (회원)
     */
    List<OrderDto> getOrdersByMember(int memCode);

    /**
     * 주문 목록 조회 (비회원)
     */
    List<OrderDto> getOrdersByGuest(String guestUuid);

    /**
     * 장바구니에서 주문 정보 계산
     */
    PaymentRequestDto calculateOrderFromCart(List<Integer> cartCodes, Integer memCode, String guestUuid);

    /**
     * 성인인증 검증
     */
    boolean verifyAdultCertification(Integer memCode, String guestUuid);

    /**
     * 결제 로그 저장
     */
    void savePaymentLog(PaymentLogDto paymentLogDto);

    /**
     * 재고 차감
     */
    boolean reduceStock(List<OrderItemDto> orderItems);

    /**
     * 재고 복구
     */
    void restoreStock(List<OrderItemDto> orderItems);

    /**
     * 장바구니 상품 삭제 (결제 완료 후)
     */
    void clearCartItems(List<Integer> cartCodes);

    /**
     * 장바구니 상품 상세 정보 조회 (상품명, 이미지, 옵션 등 포함)
     */
    List<CartDto> getCartItemsWithDetails(List<Integer> cartCodes);
}