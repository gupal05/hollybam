package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CouponDto {
    private Integer couponCode;          // coupon_code
    private String couponId;             // coupon_id
    private String couponName;           // coupon_name
    private String couponDiscription;    // coupon_discription
    private String discountType;         // discount_type ('per', 'amount')
    private Integer discountValue;       // discount_value
    private Integer minOrderPrice;       // min_order_price
    private Integer maxDiscount;         // max_discount
    private LocalDate issuedAt;          // issued_at
    private LocalDate expiredAt;         // expired_at
    private Boolean isAutoIssue;         // is_auto_issue
    private LocalDateTime createdAt;     // created_at
}
