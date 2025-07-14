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
    private String orderId;
    private LocalDateTime createAt;
    private String orderStatus;
    private String paymentMethod;
    private String ordererName;
    private String ordererPhone;
    private String ordererEmail;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddr;
    private String receiverAddrDetail;

    private int totalAmount;
    private int discountAmount;
    private int deliveryFee;
    private int finalAmount;

    private String deliveryMemo;

    private String productName;
    private String optionName;
    private String optionValue;
    private int quantity;
    private int totalPrice;

    private String imageUrl;
}