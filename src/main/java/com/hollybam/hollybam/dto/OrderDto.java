package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderDto {
    private int orderCode;
    private String orderId;
    private Integer memCode;
    private Integer guestCode;

    // 주문자 정보
    private String ordererName;
    private String ordererPhone;
    private String ordererEmail;

    // 배송 정보
    private String receiverName;
    private String receiverPhone;
    private String receiverZip;
    private String receiverAddr;
    private String receiverAddrDetail;
    private String deliveryRequest;

    // 결제 정보
    private String paymentMethod;
    private int totalAmount;
    private int discountAmount;
    private int deliveryFee;
    private int finalAmount;
    private int usePoint;
    private String depositorName;

    // PG 결제 정보
    private String pgProvider;
    private String pgTid;

    // 주문 상태
    private String orderStatus;
    private String paymentStatus;

    // 성인인증 정보
    private boolean adultVerified;
    private LocalDateTime adultVerifiedAt;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // 조인용 필드
    private List<OrderItemDto> orderItems;
    private MemberDto memberDto;
    private DeliveryDto deliveryDto;
}
