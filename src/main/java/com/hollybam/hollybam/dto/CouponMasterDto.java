package com.hollybam.hollybam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 쿠폰 마스터 DTO
 * 관리자가 생성하는 쿠폰 템플릿 정보
 */
@Data
public class CouponMasterDto {

    private Integer couponMasterCode;       // 쿠폰 마스터 고유번호
    private Integer adminCode;              // 생성한 관리자 코드
    private String couponName;              // 쿠폰명
    private String couponDescription;       // 쿠폰 설명
    private String couponType;              // 할인 타입 (FIXED: 정액, PERCENT: 정률)
    private Integer discountValue;          // 할인값
    private Integer minOrderAmount;         // 최소 주문 금액
    private Integer maxDiscountAmount;      // 최대 할인 금액
    private String applyType;               // 적용 범위 (ALL: 전체상품, SPECIFIC_PRODUCT: 특정상품)
    private Integer usageLimitPerUser;      // 사용자당 사용 제한 횟수
    private Integer totalQuantity;          // 총 발급 가능 수량
    private Integer issuedQuantity;         // 현재 발급된 수량
    private Boolean isActive;               // 활성화 여부
    private LocalDateTime issueStartDate;   // 발급 시작일
    private LocalDateTime issueEndDate;     // 발급 종료일
    private LocalDateTime useStartDate;     // 사용 시작일
    private LocalDateTime useEndDate;       // 사용 종료일
    private LocalDateTime createAt;         // 생성일시
    private LocalDateTime updateAt;         // 수정일시

    // 조인용 필드
    private String adminName;               // 관리자 이름
    private Integer remainingQuantity;      // 남은 발급 가능 수량

    // 적용 상품 목록 (특정 상품 쿠폰인 경우)
    private List<ProductDto> applicableProducts;

    // 생성자
    public CouponMasterDto() {}

    /**
     * 남은 발급 가능 수량 계산
     */
    public Integer getRemainingQuantity() {
        if (totalQuantity != null && issuedQuantity != null) {
            return totalQuantity - issuedQuantity;
        }
        return 0;
    }

    /**
     * 발급 가능 여부 확인
     */
    public boolean canIssue() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                getRemainingQuantity() > 0 &&
                issueStartDate.isBefore(now) &&
                issueEndDate.isAfter(now);
    }

    /**
     * 사용 가능 기간 여부 확인
     */
    public boolean isUsablePeriod() {
        LocalDateTime now = LocalDateTime.now();
        return useStartDate.isBefore(now) && useEndDate.isAfter(now);
    }

    /**
     * 할인 금액 계산
     */
    public Integer calculateDiscountAmount(Integer orderAmount) {
        if (orderAmount == null || orderAmount < minOrderAmount) {
            return 0;
        }

        int discountAmount = 0;

        if ("FIXED".equals(couponType)) {
            // 정액 할인
            discountAmount = discountValue;
        } else if ("PERCENT".equals(couponType)) {
            // 정률 할인
            discountAmount = (int) Math.round(orderAmount * discountValue / 100.0);

            // 최대 할인 금액 제한
            if (maxDiscountAmount != null && discountAmount > maxDiscountAmount) {
                discountAmount = maxDiscountAmount;
            }
        }

        // 주문 금액을 초과할 수 없음
        return Math.min(discountAmount, orderAmount);
    }
}
