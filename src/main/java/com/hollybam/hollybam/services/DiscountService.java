package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_DiscountDao;
import com.hollybam.hollybam.dto.DiscountDto;
import com.hollybam.hollybam.dto.DiscountCodeUsageDto;
import com.hollybam.hollybam.dto.GuestDto;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DiscountService implements IF_DiscountService {
    @Autowired
    private IF_DiscountDao discountDao;
    @Autowired
    private HttpSession httpSession;

    @Override
    @Transactional
    public void insertDiscount(DiscountDto discountDto) {
        discountDao.insertDiscount(discountDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> validateDiscountCode(String discountId, Long orderAmount, Integer code) throws Exception {

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

        // 할인코드 유효성 검사 (만료일 확인)
        validateDiscountAvailability(discount);

        // null 체크
        if (discount.getMinOrderPrice() == null) {
            discount.setMinOrderPrice(0);
        }

        // 🆕 중복 사용 여부 확인 (회원인 경우만)
        if(httpSession.getAttribute("member") != null){
            int usageCount = discountDao.checkDiscountCodeUsage(discount.getDiscountCode(), code);
            if (usageCount > 0) {
                throw new RuntimeException("이미 사용한 할인코드입니다. 할인코드는 회원당 1회만 사용 가능합니다.");
            }
            log.info("할인코드 중복 사용 체크 완료: discountId={}, memCode={}, usageCount={}",
                    discountId, code, usageCount);
        } else {
            GuestDto guest = (GuestDto)httpSession.getAttribute("guest");
            int usageCount = discountDao.checkDiscountCodeUsageForGuest(discount.getDiscountCode(), guest.getGuestCode());
            if (usageCount > 0) {
                throw new RuntimeException("이미 사용한 할인코드입니다. 할인코드는 회원당 1회만 사용 가능합니다.");
            }
            log.info("할인코드 중복 사용 체크 완료: discountId={}, guestCode={}, usageCount={}",
                    discountId, guest.getGuestCode(), usageCount);
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

        if(httpSession.getAttribute("member") != null) {
            log.info("할인코드 검증 완료: discountId={}, memCode={}, discountAmount={}",
                    discountId, code, discountAmount);
        } else {
            GuestDto guest = (GuestDto)httpSession.getAttribute("guest");
            log.info("할인코드 검증 완료: discountId={}, guestCode={}, discountAmount={}",
                    discountId, guest.getGuestCode(), discountAmount);
        }


        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountDto getDiscountByCode(String discountId) throws Exception {
        if (discountId == null || discountId.trim().isEmpty()) {
            return null;
        }
        return discountDao.selectDiscountByCode(discountId.trim().toUpperCase());
    }

    @Override
    @Transactional
    public void recordDiscountCodeUsage(Integer discountCode, Integer memCode) throws Exception {
        if (discountCode == null || memCode == null) {
            log.warn("할인코드 사용 내역 저장 건너뜀: discountCode={}, memCode={}", discountCode, memCode);
            return;
        }

        try {
            // 중복 사용 체크 (안전장치)
            int existingUsage = discountDao.checkDiscountCodeUsage(discountCode, memCode);
            if (existingUsage > 0) {
                log.warn("이미 사용한 할인코드 사용 내역 저장 시도: discountCode={}, memCode={}", discountCode, memCode);
                return;
            }

            DiscountCodeUsageDto usageDto = new DiscountCodeUsageDto();
            usageDto.setDiscountCode(discountCode);
            usageDto.setMemCode(memCode);
            usageDto.setUsedAt(LocalDateTime.now());

            int result = discountDao.insertDiscountCodeUsage(usageDto);

            if (result > 0) {
                log.info("할인코드 사용 내역 저장 완료: discountCode={}, memCode={}, usageCode={}",
                        discountCode, memCode, usageDto.getUsageCode());
            } else {
                log.error("할인코드 사용 내역 저장 실패: discountCode={}, memCode={}", discountCode, memCode);
            }

        } catch (Exception e) {
            log.error("할인코드 사용 내역 저장 중 오류 발생: discountCode={}, memCode={}", discountCode, memCode, e);
            // 중복 키 제약 조건 위반인 경우 무시 (이미 사용한 할인코드)
            if (e.getMessage() != null &&
                    (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("duplicate key"))) {
                log.warn("할인코드 중복 사용 시도 감지: discountCode={}, memCode={}", discountCode, memCode);
            } else {
                // 다른 오류는 상위로 전파하지 않음 (주문 실패 방지)
                log.error("할인코드 사용 내역 저장 예상치 못한 오류", e);
            }
        }
    }

    @Override
    @Transactional
    public void recordDiscountCodeUsageForGuest(Integer discountCode, Integer guestCode) throws Exception {
        if (discountCode == null || guestCode == null) {
            log.warn("할인코드 사용 내역 저장 건너뜀: discountCode={}, memCode={}", discountCode, guestCode);
            return;
        }

        try {
            // 중복 사용 체크 (안전장치)
            int existingUsage = discountDao.checkDiscountCodeUsage(discountCode, guestCode);
            if (existingUsage > 0) {
                log.warn("이미 사용한 할인코드 사용 내역 저장 시도: discountCode={}, guestCode={}", discountCode, guestCode);
                return;
            }

            DiscountCodeUsageDto usageDto = new DiscountCodeUsageDto();
            usageDto.setDiscountCode(discountCode);
            usageDto.setGuestCode(guestCode);
            usageDto.setUsedAt(LocalDateTime.now());

            int result = discountDao.insertDiscountCodeUsageForGuest(usageDto);

            if (result > 0) {
                log.info("할인코드 사용 내역 저장 완료: discountCode={}, memCode={}, usageCode={}",
                        discountCode, guestCode, usageDto.getUsageCode());
            } else {
                log.error("할인코드 사용 내역 저장 실패: discountCode={}, memCode={}", discountCode, guestCode);
            }

        } catch (Exception e) {
            log.error("할인코드 사용 내역 저장 중 오류 발생: discountCode={}, guestCode={}", discountCode, guestCode, e);
            // 중복 키 제약 조건 위반인 경우 무시 (이미 사용한 할인코드)
            if (e.getMessage() != null &&
                    (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("duplicate key"))) {
                log.warn("할인코드 중복 사용 시도 감지: discountCode={}, guestCode={}", discountCode, guestCode);
            } else {
                // 다른 오류는 상위로 전파하지 않음 (주문 실패 방지)
                log.error("할인코드 사용 내역 저장 예상치 못한 오류", e);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountCodeUsageDto> getDiscountUsageHistory(Integer memCode) throws Exception {
        if (memCode == null) {
            throw new IllegalArgumentException("회원 코드가 필요합니다.");
        }

        return discountDao.selectDiscountUsageByMember(memCode);
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
        Long discountAmount = 0L;

        if ("per".equals(discount.getDiscountType())) {
            // 퍼센트 할인
            discountAmount = Math.round(orderAmount * discount.getDiscountValue() / 100.0);
        } else if ("amount".equals(discount.getDiscountType())) {
            // 고정 금액 할인
            discountAmount = Long.valueOf(discount.getDiscountValue());
        }

        // 할인금액이 주문금액을 초과하지 않도록 제한
        if (discountAmount > orderAmount) {
            discountAmount = orderAmount;
        }

        return discountAmount;
    }
}