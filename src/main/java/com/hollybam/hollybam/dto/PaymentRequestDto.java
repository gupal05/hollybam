package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
public class PaymentRequestDto {
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

    // 장바구니 정보
    private List<Integer> cartCodes;

    // 성인인증 정보
    private boolean adultVerified;
    private LocalDate buyerBirth;

    // 약관 동의
    private boolean agreeTerms;
    private boolean agreePrivacy;
    private boolean agreeMarketing;

    // 사용자 구분
    private Integer memberCode;
    private Integer guestCode;
}
