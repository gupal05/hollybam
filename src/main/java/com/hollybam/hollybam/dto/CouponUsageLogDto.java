package com.hollybam.hollybam.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 쿠폰 사용 내역 DTO
 * 쿠폰 사용 로그 관리
 */
@Data
public class CouponUsageLogDto {

    private Integer usageLogCode;           // 사용 내역 고유번호
    private Integer memberCouponCode;       // 회원 쿠폰 코드
    private Integer orderCode;              // 주문 코드
    private Integer discountAmount;         // 실제 할인된 금액
    private Integer originalOrderAmount;    // 쿠폰 적용 전 주문 금액
    private Integer finalOrderAmount;       // 쿠폰 적용 후 주문 금액
    private LocalDateTime usedAt;           // 사용일시

    // 조인용 필드
    private String couponCode;              // 쿠폰 코드
    private String couponName;              // 쿠폰명
    private String memberName;              // 회원 이름
    private String orderId;                 // 주문 ID

    // 생성자
    public CouponUsageLogDto() {}

    public CouponUsageLogDto(Integer memberCouponCode, Integer orderCode,
                             Integer discountAmount, Integer originalOrderAmount,
                             Integer finalOrderAmount) {
        this.memberCouponCode = memberCouponCode;
        this.orderCode = orderCode;
        this.discountAmount = discountAmount;
        this.originalOrderAmount = originalOrderAmount;
        this.finalOrderAmount = finalOrderAmount;
    }
}