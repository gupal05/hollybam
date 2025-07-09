package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 목록 표시용 DTO
 * 주문 정보와 함께 주문 상품들의 정보를 포함
 */
@Getter
@Setter
@ToString
public class OrderListDto {
    // 주문 기본 정보
    private int orderCode;
    private String orderId;
    private String orderNumber; // 주문번호 (orderId와 동일하지만 페이지에서 사용하는 명칭)
    private int memCode;
    private int guestCode;
    private String ordererName;
    private String ordererPhone;
    private String ordererEmail;

    // 배송 정보
    private String receiverName;
    private String receiverPhone;
    private String receiverAddr;

    // 결제 정보
    private String paymentMethod;
    private int totalAmount;
    private int discountAmount;
    private int deliveryFee;
    private int finalAmount;

    // 주문 상태
    private String orderStatus;
    private String paymentStatus;

    // 배송 정보
    private String trackingNumber;
    private String deliveryStatus;

    // 시간 정보
    private LocalDateTime orderDate; // createAt을 orderDate로 매핑
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // 주문 상품 목록
    private List<OrderProductDto> products;

    // 편의 메서드들
    public String getOrderNumber() {
        return this.orderId != null ? this.orderId : this.orderNumber;
    }

    public LocalDateTime getOrderDate() {
        return this.createAt != null ? this.createAt : this.orderDate;
    }

    /**
     * 주문 상품 정보를 담는 내부 DTO
     */
    @Getter
    @Setter
    @ToString
    public static class OrderProductDto {
        private int productCode;
        private String productName;
        private String productImage;
        private int productPrice;
        private int quantity;
        private String options; // 옵션 정보 (예: "색상-레드, 사이즈-M")
        private int unitPrice;
        private int optionPrice;
        private int totalPrice;

        // 옵션 정보 조합 메서드
        public String getOptions() {
            return this.options != null && !this.options.trim().isEmpty() ? this.options : null;
        }
    }
}