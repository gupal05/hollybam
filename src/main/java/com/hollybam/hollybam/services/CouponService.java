package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CouponDao;
import com.hollybam.hollybam.dto.CouponDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CouponService implements IF_CouponService {
    private final IF_CouponDao couponDao;

    public CouponService(IF_CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    @Override
    public void insertCoupon(CouponDto couponDto) {
        couponDao.insertCoupon(couponDto);
    }

    @Override
    public List<CouponDto> getUsePossibleCoupon(int memberId) {
        return couponDao.getUsePossibleCoupon(memberId);
    }

    @Override
    public int selectCouponCount(int memberCode) {
        return couponDao.selectCouponCount(memberCode);
    }

    @Override
    public int selectTotalCouponCount(int memberCode){
        return couponDao.selectTotalCouponCount(memberCode);
    }

    @Override
    public int selectUsedCouponCount(int memberCode){
        return couponDao.selectUsedCouponCount(memberCode);
    }

    @Override
    public int expirationCouponCount(int memberCode){
        return couponDao.expirationCouponCount(memberCode);
    }

    @Override
    public List<Map<String, Object>> selectMemberCouponList(int memCode, String status) {
        return couponDao.selectMemberCouponList(memCode, status);
    }

    @Override
    @Transactional
    public boolean useCoupon(int couponMemberCode, int orderCode) {
        try {
            return couponDao.useCoupon(couponMemberCode, orderCode) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public int getCouponMemberCode(int memberCode, int couponCode){
        return couponDao.getCouponMemberCode(memberCode, couponCode);
    }

    /**
     * 간단한 쿠폰 등록 처리
     */
    @Transactional
    public Map<String, Object> registerCoupon(String couponId, int memberCode) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 쿠폰 번호 입력 확인
            if (couponId == null || couponId.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "쿠폰 번호를 입력해주세요.");
                return result;
            }

            couponId = couponId.trim();

            // 2. 쿠폰 번호 형식 확인 (xxxx-xxxx-xxxx)
            if (couponId.length() != 14) {
                result.put("success", false);
                result.put("message", "올바른 쿠폰 번호 형식을 입력해주세요. (xxxx-xxxx-xxxx)");
                return result;
            }

            // 3. 하이픈 위치 확인
            if (couponId.charAt(4) != '-' || couponId.charAt(9) != '-') {
                result.put("success", false);
                result.put("message", "올바른 쿠폰 번호 형식을 입력해주세요. (xxxx-xxxx-xxxx)");
                return result;
            }

            // 4. 쿠폰이 존재하는지 확인 (하이픈 포함된 형태로 조회)
            CouponDto coupon = couponDao.findCouponByCouponId(couponId);
            if (coupon == null) {
                result.put("success", false);
                result.put("message", "존재하지 않는 쿠폰 번호입니다.");
                return result;
            }

            // 5. 이미 가지고 있는 쿠폰인지 확인
            int hasCount = couponDao.checkMemberHasCoupon(memberCode, coupon.getCouponCode());
            if (hasCount > 0) {
                result.put("success", false);
                result.put("message", "이미 등록된 쿠폰입니다.");
                return result;
            }

            // 6. 쿠폰 발급
            int insertResult = couponDao.giveCouponToMember(memberCode, coupon.getCouponCode());

            if (insertResult > 0) {
                result.put("success", true);
                result.put("message", "🎉 " + coupon.getCouponName() + " 쿠폰이 성공적으로 등록되었습니다!");
                result.put("couponName", coupon.getCouponName());
            } else {
                result.put("success", false);
                result.put("message", "쿠폰 등록에 실패했습니다.");
            }

        } catch (Exception e) {
            log.error("쿠폰 등록 오류: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "쿠폰 등록 중 오류가 발생했습니다.");
        }

        return result;
    }
}
