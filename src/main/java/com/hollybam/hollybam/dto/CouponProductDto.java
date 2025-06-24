package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CouponProductDto {
    private Integer couponProductCode;  // coupon_product_code
    private Integer couponCode;         // coupon_code (FK)
    private Integer productCode;        // product_code (FK)
}
