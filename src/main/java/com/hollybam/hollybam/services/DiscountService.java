package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_DiscountDao;
import com.hollybam.hollybam.dto.DiscountDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DiscountService implements  IF_DiscountService{
    @Autowired
    private IF_DiscountDao discountDao;

    @Override
    public void insertDiscount(DiscountDto discountDto) {
        discountDao.insertDiscount(discountDto);
    }

    @Override
    public Map<String, Object> validateDiscountCode(String discountId, Long orderAmount) throws Exception {

        // 입력값 검증
        if (discountId == null || discountId.trim().isEmpty()) {
            throw new IllegalArgumentException("할인코드를 입력해주세요.");
        }

        if (orderAmount == null || orderAmount <= 0) {
            throw new IllegalArgumentException("올바른 주문금액이 아닙니다.");
        }

        // 할인코드 대문자 변환
        discountId = discountId.trim().toUpperCase();

        // 할인코드 조회
        DiscountDto discount = discountDao.selectDiscountByCode(discountId);
        if (discount == null) {
            throw new RuntimeException("유효하지 않은 할인코드입니다.");
        }

        // 할인코드 유효성 검사
        validateDiscountAvailability(discount);
        if (discount.getMinOrderPrice() == null) {
            discount.setMinOrderPrice(0);
        }

        // 최소 주문금액 확인
        if (orderAmount < discount.getMinOrderPrice()) {
            throw new RuntimeException("최소 주문금액이 부족합니다. " +
                    String.format("%,d", discount.getMinOrderPrice()) + "원 이상 주문해주세요.");
        }

        // 할인금액 계산
        Long discountAmount = calculateDiscountAmount(discount, orderAmount);

        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("discountInfo", discount);
        result.put("discountAmount", discountAmount);

        log.info("할인코드 검증 완료: discountId={}, discountAmount={}", discountId, discountAmount);

        return result;
    }

    @Override
    public DiscountDto getDiscountByCode(String discountId) throws Exception {
        return discountDao.selectDiscountByCode(discountId);
    }

    /**
     * 할인코드 유효성 검사
     */
    private void validateDiscountAvailability(DiscountDto discount) {
        LocalDate now = LocalDate.now();

        // 만료일 확인
        if (discount.getExpiredAt() != null && now.isAfter(discount.getExpiredAt())) {
            throw new RuntimeException("만료된 할인코드입니다.");
        }
    }

    /**
     * 할인금액 계산
     */
    private Long calculateDiscountAmount(DiscountDto discount, Long orderAmount) {
        if ("per".equals(discount.getDiscountType())) {
            // 퍼센트 할인
            return orderAmount * discount.getDiscountValue() / 100;

        } else if ("amount".equals(discount.getDiscountType())) {
            // 고정 금액 할인 (주문금액보다 클 수 없음)
            return Math.min(discount.getDiscountValue().longValue(), orderAmount);

        } else {
            throw new IllegalArgumentException("알 수 없는 할인 타입: " + discount.getDiscountType());
        }
    }
}
