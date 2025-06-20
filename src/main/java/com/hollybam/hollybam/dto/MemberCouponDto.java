package com.hollybam.hollybam.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 회원 쿠폰함 DTO
 * 실제 발급된 개별 쿠폰 정보
 */
@Data
public class MemberCouponDto {

    private Integer memberCouponCode;       // 회원 쿠폰 고유번호
    private Integer couponMasterCode;       // 쿠폰 마스터 코드
    private Integer memCode;                // 회원 코드
    private String couponCode;              // 쿠폰 코드 (고유한 쿠폰 번호)
    private String couponStatus;            // 쿠폰 상태 (ISSUED, USED, EXPIRED)
    private Integer orderCode;              // 사용된 주문 코드
    private LocalDateTime issuedAt;         // 발급일시
    private LocalDateTime usedAt;           // 사용일시
    private LocalDateTime expiredAt;        // 만료일시
    private LocalDateTime createAt;         // 생성일시
    private LocalDateTime updateAt;         // 수정일시

    // 조인용 필드 (쿠폰 마스터 정보)
    private String couponName;              // 쿠폰명
    private String couponDescription;       // 쿠폰 설명
    private String couponType;              // 할인 타입
    private Integer discountValue;          // 할인값
    private Integer minOrderAmount;         // 최소 주문 금액
    private Integer maxDiscountAmount;      // 최대 할인 금액
    private String applyType;               // 적용 범위
    private LocalDateTime useStartDate;     // 사용 시작일
    private LocalDateTime useEndDate;       // 사용 종료일

    // 조인용 필드 (회원 정보)
    private String memberName;              // 회원 이름
    private String memberId;                // 회원 아이디

    // 생성자
    public MemberCouponDto() {}

    /**
     * 쿠폰 사용 가능 여부 확인
     */
    public boolean isUsable() {
        LocalDateTime now = LocalDateTime.now();
        return "ISSUED".equals(couponStatus) &&
                expiredAt.isAfter(now) &&
                useStartDate.isBefore(now) &&
                useEndDate.isAfter(now);
    }

    /**
     * 쿠폰 만료 여부 확인
     */
    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now()) || "EXPIRED".equals(couponStatus);
    }

    /**
     * D-Day 계산 (만료까지 남은 일수)
     */
    public Long getDaysUntilExpiry() {
        if (isExpired()) {
            return 0L;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiredAt);
    }

    /**
     * 할인 금액 계산
     */
    public Integer calculateDiscountAmount(Integer orderAmount) {
        if (!isUsable() || orderAmount == null || orderAmount < minOrderAmount) {
            return 0;
        }

        int discountAmount = 0;

        if ("FIXED".equals(couponType)) {
            discountAmount = discountValue;
        } else if ("PERCENT".equals(couponType)) {
            discountAmount = (int) Math.round(orderAmount * discountValue / 100.0);
            if (maxDiscountAmount != null && discountAmount > maxDiscountAmount) {
                discountAmount = maxDiscountAmount;
            }
        }

        return Math.min(discountAmount, orderAmount);
    }
}