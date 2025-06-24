package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CouponMemberDto {
    private Integer couponMemberCode;   // coupon_member_code
    private Integer couponCode;         // coupon_code (FK)
    private Integer memCode;            // mem_code (FK)
    private LocalDateTime issuedAt;     // issued_at (발급 시각)
    private Boolean used;               // used (0: 미사용, 1: 사용)
    private LocalDateTime usedAt;       // used_at (사용 시각)
    private Integer orderCode;          // order_code (FK, 사용된 주문 코드)
}
