package com.hollybam.hollybam.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 쿠폰 적용 상품 DTO
 * 특정 상품에만 적용되는 쿠폰용
 */
@Data
public class CouponApplicableProductDto {

    private Integer couponProductCode;      // 쿠폰 적용 상품 고유번호
    private Integer couponMasterCode;       // 쿠폰 마스터 코드
    private Integer productCode;            // 적용 상품 코드
    private LocalDateTime createAt;         // 생성일시

    // 조인용 필드
    private String productName;             // 상품명
    private String productId;               // 상품 ID
    private Integer productPrice;           // 상품 가격

    // 생성자
    public CouponApplicableProductDto() {}

    public CouponApplicableProductDto(Integer couponMasterCode, Integer productCode) {
        this.couponMasterCode = couponMasterCode;
        this.productCode = productCode;
    }
}