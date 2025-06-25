package com.hollybam.hollybam.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 쿠폰 관련 유틸리티 클래스
 */
public class CouponUtils {

    /**
     * 쿠폰 상태 계산
     * @param used 사용 여부
     * @param expiredAt 만료일
     * @return 쿠폰 상태 ("available", "used", "expired")
     */
    public static String calculateCouponStatus(Boolean used, LocalDate expiredAt) {
        if (used != null && used) {
            return "used";
        }

        if (expiredAt != null && expiredAt.isBefore(LocalDate.now())) {
            return "expired";
        }

        return "available";
    }

    /**
     * 할인 금액 표시 형식 변환
     * @param discountType 할인 타입 ("per", "amount")
     * @param discountValue 할인 값
     * @return 포맷된 할인 표시 문자열
     */
    public static String formatDiscountDisplay(String discountType, Integer discountValue) {
        if (discountValue == null) {
            return "0";
        }

        if ("per".equals(discountType)) {
            return discountValue + "%";
        } else if ("amount".equals(discountType)) {
            return String.format("%,d원", discountValue);
        }

        return discountValue.toString();
    }

    /**
     * 쿠폰 만료까지 남은 일수 계산
     * @param expiredAt 만료일
     * @return 남은 일수 (음수면 이미 만료)
     */
    public static long getDaysUntilExpiry(LocalDate expiredAt) {
        if (expiredAt == null) {
            return -1;
        }

        return LocalDate.now().until(expiredAt).getDays();
    }

    /**
     * 쿠폰 사용 가능 여부 확인
     * @param couponData 쿠폰 데이터 맵
     * @param orderAmount 주문 금액
     * @return 사용 가능 여부
     */
    public static boolean isCouponUsable(Map<String, Object> couponData, Integer orderAmount) {
        // 이미 사용된 쿠폰인지 확인
        Boolean used = (Boolean) couponData.get("used");
        if (used != null && used) {
            return false;
        }

        // 만료된 쿠폰인지 확인
        LocalDate expiredAt = (LocalDate) couponData.get("expired_at");
        if (expiredAt != null && expiredAt.isBefore(LocalDate.now())) {
            return false;
        }

        // 최소 주문 금액 확인
        Integer minOrderPrice = (Integer) couponData.get("min_order_price");
        if (minOrderPrice != null && orderAmount != null && orderAmount < minOrderPrice) {
            return false;
        }

        return true;
    }

    /**
     * 할인 금액 계산
     * @param couponData 쿠폰 데이터
     * @param orderAmount 주문 금액
     * @return 실제 할인 금액
     */
    public static int calculateDiscountAmount(Map<String, Object> couponData, int orderAmount) {
        String discountType = (String) couponData.get("discount_type");
        Integer discountValue = (Integer) couponData.get("discount_value");
        Integer maxDiscount = (Integer) couponData.get("max_discount");

        if (discountValue == null) {
            return 0;
        }

        int discountAmount = 0;

        if ("per".equals(discountType)) {
            // 퍼센트 할인
            discountAmount = (int) Math.round(orderAmount * discountValue / 100.0);
        } else if ("amount".equals(discountType)) {
            // 정액 할인
            discountAmount = discountValue;
        }

        // 최대 할인 금액 제한 적용
        if (maxDiscount != null && discountAmount > maxDiscount) {
            discountAmount = maxDiscount;
        }

        // 주문 금액을 초과할 수 없음
        if (discountAmount > orderAmount) {
            discountAmount = orderAmount;
        }

        return discountAmount;
    }

    /**
     * 날짜 포맷팅 (화면 표시용)
     * @param date 날짜
     * @return 포맷된 날짜 문자열 (yyyy.MM.dd)
     */
    public static String formatDateForDisplay(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    /**
     * 날짜시간 포맷팅 (화면 표시용)
     * @param dateTime 날짜시간
     * @return 포맷된 날짜시간 문자열 (yyyy.MM.dd HH:mm)
     */
    public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
    }
}
