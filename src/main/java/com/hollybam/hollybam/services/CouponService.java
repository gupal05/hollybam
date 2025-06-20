package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_CouponDao;
import com.hollybam.hollybam.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 쿠폰 서비스 구현체 (결제 연동 제외)
 */
@Service
public class CouponService implements IF_CouponService {

    @Autowired
    private IF_CouponDao couponDao;

    // ===============================
    // 관리자 기능
    // ===============================

    @Override
    @Transactional
    public boolean createCouponMaster(CouponMasterDto couponMasterDto, List<Integer> applicableProductCodes, Integer adminCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(adminCode)) {
            throw new Exception("관리자만 쿠폰을 생성할 수 있습니다.");
        }

        // 입력 데이터 검증
        validateCouponMasterData(couponMasterDto);

        couponMasterDto.setAdminCode(adminCode);

        // 쿠폰 마스터 등록
        int result = couponDao.insertCouponMaster(couponMasterDto);

        // 특정 상품 쿠폰인 경우 적용 상품 등록
        if ("SPECIFIC_PRODUCT".equals(couponMasterDto.getApplyType()) &&
                applicableProductCodes != null && !applicableProductCodes.isEmpty()) {

            List<CouponApplicableProductDto> products = new ArrayList<>();
            for (Integer productCode : applicableProductCodes) {
                products.add(new CouponApplicableProductDto(couponMasterDto.getCouponMasterCode(), productCode));
            }
            couponDao.insertCouponApplicableProducts(products);
        }

        return result > 0;
    }

    @Override
    @Transactional
    public boolean updateCouponMaster(CouponMasterDto couponMasterDto, List<Integer> applicableProductCodes, Integer adminCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(adminCode)) {
            throw new Exception("관리자만 쿠폰을 수정할 수 있습니다.");
        }

        // 입력 데이터 검증
        validateCouponMasterData(couponMasterDto);

        // 쿠폰 마스터 수정
        int result = couponDao.updateCouponMaster(couponMasterDto);

        // 특정 상품 쿠폰인 경우 적용 상품 재등록
        if ("SPECIFIC_PRODUCT".equals(couponMasterDto.getApplyType())) {
            // 기존 적용 상품 삭제
            couponDao.deleteCouponApplicableProducts(couponMasterDto.getCouponMasterCode());

            // 새로운 적용 상품 등록
            if (applicableProductCodes != null && !applicableProductCodes.isEmpty()) {
                List<CouponApplicableProductDto> products = new ArrayList<>();
                for (Integer productCode : applicableProductCodes) {
                    products.add(new CouponApplicableProductDto(couponMasterDto.getCouponMasterCode(), productCode));
                }
                couponDao.insertCouponApplicableProducts(products);
            }
        } else {
            // 전체 상품 쿠폰으로 변경되는 경우 기존 적용 상품 삭제
            couponDao.deleteCouponApplicableProducts(couponMasterDto.getCouponMasterCode());
        }

        return result > 0;
    }

    @Override
    @Transactional
    public boolean deleteCouponMaster(Integer couponMasterCode, Integer adminCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(adminCode)) {
            throw new Exception("관리자만 쿠폰을 삭제할 수 있습니다.");
        }

        // 이미 발급된 쿠폰이 있는지 확인
        CouponMasterDto couponMaster = couponDao.selectCouponMasterDetail(couponMasterCode);
        if (couponMaster != null && couponMaster.getIssuedQuantity() > 0) {
            throw new Exception("이미 발급된 쿠폰이 있어 삭제할 수 없습니다. 비활성화를 권장합니다.");
        }

        return couponDao.deleteCouponMaster(couponMasterCode) > 0;
    }

    @Override
    @Transactional
    public boolean issueCouponToMember(Integer couponMasterCode, Integer memCode, Integer adminCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(adminCode)) {
            throw new Exception("관리자만 쿠폰을 발급할 수 있습니다.");
        }

        return issueCouponByMember(couponMasterCode, memCode);
    }

    @Override
    @Transactional
    public int issueCouponToMultipleMembers(Integer couponMasterCode, List<Integer> memCodes, Integer adminCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(adminCode)) {
            throw new Exception("관리자만 쿠폰을 발급할 수 있습니다.");
        }

        int issuedCount = 0;
        List<String> failedMembers = new ArrayList<>();

        for (Integer memCode : memCodes) {
            try {
                if (issueCouponByMember(couponMasterCode, memCode)) {
                    issuedCount++;
                }
            } catch (Exception e) {
                failedMembers.add("회원코드 " + memCode + ": " + e.getMessage());
            }
        }

        // 실패한 발급이 있으면 로그로 기록
        if (!failedMembers.isEmpty()) {
            System.out.println("쿠폰 발급 실패 목록: " + String.join(", ", failedMembers));
        }

        return issuedCount;
    }

    // ===============================
    // 쿠폰 조회 기능
    // ===============================

    @Override
    public CouponMasterDto getCouponMasterDetail(Integer couponMasterCode) {
        CouponMasterDto couponMaster = couponDao.selectCouponMasterDetail(couponMasterCode);

        // 특정 상품 쿠폰인 경우 적용 상품 목록도 조회
        if (couponMaster != null && "SPECIFIC_PRODUCT".equals(couponMaster.getApplyType())) {
            List<CouponApplicableProductDto> applicableProducts =
                    couponDao.selectCouponApplicableProducts(couponMasterCode);
            // 필요시 ProductDto로 변환하는 로직 추가 가능
        }

        return couponMaster;
    }

    @Override
    public List<CouponMasterDto> getActiveCouponMasterList(int page, int size) {
        int offset = (page - 1) * size;
        return couponDao.selectActiveCouponMasterList(offset, size);
    }

    @Override
    public List<CouponMasterDto> getAllCouponMasterList(int page, int size) {
        int offset = (page - 1) * size;
        return couponDao.selectAllCouponMasterList(offset, size);
    }

    @Override
    public List<CouponMasterDto> getIssuableCouponList() {
        return couponDao.selectIssuableCouponMasterList();
    }

    @Override
    public int getCouponMasterCount(Boolean isActive) {
        return couponDao.selectCouponMasterCount(isActive);
    }

    // ===============================
    // 회원 쿠폰 기능
    // ===============================

    @Override
    @Transactional
    public boolean issueCouponByMember(Integer couponMasterCode, Integer memCode) throws Exception {
        // 쿠폰 마스터 정보 조회
        CouponMasterDto couponMaster = couponDao.selectCouponMasterDetail(couponMasterCode);
        if (couponMaster == null) {
            throw new Exception("존재하지 않는 쿠폰입니다.");
        }

        // 발급 가능 여부 확인
        if (!couponMaster.canIssue()) {
            throw new Exception("발급 기간이 아니거나 발급 수량이 소진된 쿠폰입니다.");
        }

        // 사용자당 발급 제한 확인
        int existingCount = couponDao.checkMemberCouponExists(memCode, couponMasterCode);
        if (existingCount >= couponMaster.getUsageLimitPerUser()) {
            throw new Exception("이미 발급 가능한 횟수를 초과했습니다.");
        }

        // 쿠폰 코드 생성
        String couponCode = generateUniqueCouponCode();

        // 회원 쿠폰 생성
        MemberCouponDto memberCoupon = new MemberCouponDto();
        memberCoupon.setCouponMasterCode(couponMasterCode);
        memberCoupon.setMemCode(memCode);
        memberCoupon.setCouponCode(couponCode);
        memberCoupon.setCouponStatus("ISSUED");
        memberCoupon.setExpiredAt(couponMaster.getUseEndDate()); // 사용 종료일까지

        // 회원 쿠폰 등록
        int result = couponDao.insertMemberCoupon(memberCoupon);

        if (result > 0) {
            // 발급 수량 증가
            couponDao.increaseCouponIssuedQuantity(couponMasterCode);
            return true;
        }

        return false;
    }

    @Override
    public List<MemberCouponDto> getMemberUsableCoupons(Integer memCode) {
        return couponDao.selectUsableMemberCoupons(memCode);
    }

    @Override
    public List<MemberCouponDto> getMemberCoupons(Integer memCode, String status, int page, int size) {
        int offset = (page - 1) * size;
        return couponDao.selectMemberCoupons(memCode, status, offset, size);
    }

    @Override
    public int getMemberCouponCount(Integer memCode, String status) {
        return couponDao.selectMemberCouponCount(memCode, status);
    }

    @Override
    public List<MemberCouponDto> getUsableCouponsForProduct(Integer memCode, Integer productCode) {
        return couponDao.selectUsableMemberCouponsForProduct(memCode, productCode);
    }

    @Override
    public List<MemberCouponDto> getUsableCouponsForCart(Integer memCode, List<Integer> productCodes) {
        if (productCodes == null || productCodes.isEmpty()) {
            return getMemberUsableCoupons(memCode); // 전체 상품 쿠폰만 반환
        }
        return couponDao.selectUsableMemberCouponsForCart(memCode, productCodes);
    }

    @Override
    public MemberCouponDto getMemberCouponByCouponCode(String couponCode, Integer memCode) {
        return couponDao.selectMemberCouponByCouponCode(couponCode, memCode);
    }

    // ===============================
    // 쿠폰 할인 계산 (결제 연동 없음)
    // ===============================

    @Override
    public Integer calculateCouponDiscount(String couponCode, Integer memCode, List<Integer> productCodes, Integer orderAmount) throws Exception {
        MemberCouponDto memberCoupon = couponDao.selectMemberCouponByCouponCode(couponCode, memCode);
        if (memberCoupon == null || !memberCoupon.isUsable()) {
            return 0;
        }

        // 최소 주문 금액 확인
        if (orderAmount < memberCoupon.getMinOrderAmount()) {
            return 0;
        }

        // 특정 상품 쿠폰인 경우 상품 적용 가능 여부 확인
        if ("SPECIFIC_PRODUCT".equals(memberCoupon.getApplyType())) {
            List<CouponApplicableProductDto> applicableProducts =
                    couponDao.selectCouponApplicableProducts(memberCoupon.getCouponMasterCode());

            boolean hasApplicableProduct = applicableProducts.stream()
                    .anyMatch(ap -> productCodes.contains(ap.getProductCode()));

            if (!hasApplicableProduct) {
                return 0; // 적용 가능한 상품이 없음
            }
        }

        return memberCoupon.calculateDiscountAmount(orderAmount);
    }

    @Override
    public boolean validateCouponUsage(String couponCode, Integer memCode, List<Integer> productCodes, Integer orderAmount) throws Exception {
        MemberCouponDto memberCoupon = couponDao.selectMemberCouponByCouponCode(couponCode, memCode);

        if (memberCoupon == null) {
            throw new Exception("존재하지 않는 쿠폰이거나 본인의 쿠폰이 아닙니다.");
        }

        if (!memberCoupon.isUsable()) {
            throw new Exception("사용할 수 없는 쿠폰입니다. (상태: " + memberCoupon.getCouponStatus() + ")");
        }

        if (orderAmount < memberCoupon.getMinOrderAmount()) {
            throw new Exception("최소 주문 금액(" + formatPrice(memberCoupon.getMinOrderAmount()) + "원)을 충족하지 않습니다.");
        }

        // 특정 상품 쿠폰인 경우 적용 가능 상품 확인
        if ("SPECIFIC_PRODUCT".equals(memberCoupon.getApplyType())) {
            List<CouponApplicableProductDto> applicableProducts =
                    couponDao.selectCouponApplicableProducts(memberCoupon.getCouponMasterCode());

            boolean hasApplicableProduct = applicableProducts.stream()
                    .anyMatch(ap -> productCodes.contains(ap.getProductCode()));

            if (!hasApplicableProduct) {
                throw new Exception("이 쿠폰은 장바구니의 상품에 적용할 수 없습니다.");
            }
        }

        return true;
    }

    // ===============================
    // 통계 및 관리
    // ===============================

    @Override
    @Transactional
    public int processExpiredCoupons() {
        return couponDao.updateExpiredCoupons();
    }

    @Override
    public boolean isAdminUser(Integer memberCode) {
        if (memberCode == null) {
            return false;
        }
        return couponDao.isAdmin(memberCode) > 0;
    }

    @Override
    public int calculateTotalPages(int totalCount, int size) {
        return (int) Math.ceil((double) totalCount / size);
    }

    // ===============================
    // 유틸리티 메서드
    // ===============================

    /**
     * 쿠폰 마스터 데이터 유효성 검증
     */
    private void validateCouponMasterData(CouponMasterDto couponMasterDto) throws Exception {
        if (couponMasterDto.getCouponName() == null || couponMasterDto.getCouponName().trim().isEmpty()) {
            throw new Exception("쿠폰명을 입력해주세요.");
        }

        if (couponMasterDto.getDiscountValue() == null || couponMasterDto.getDiscountValue() <= 0) {
            throw new Exception("할인값은 0보다 커야 합니다.");
        }

        if ("PERCENT".equals(couponMasterDto.getCouponType()) && couponMasterDto.getDiscountValue() > 100) {
            throw new Exception("할인율은 100%를 초과할 수 없습니다.");
        }

        if (couponMasterDto.getTotalQuantity() == null || couponMasterDto.getTotalQuantity() <= 0) {
            throw new Exception("발급 가능 수량을 올바르게 입력해주세요.");
        }

        if (couponMasterDto.getIssueStartDate() == null || couponMasterDto.getIssueEndDate() == null ||
                couponMasterDto.getUseStartDate() == null || couponMasterDto.getUseEndDate() == null) {
            throw new Exception("발급 기간과 사용 기간을 모두 입력해주세요.");
        }

        if (couponMasterDto.getIssueStartDate().isAfter(couponMasterDto.getIssueEndDate())) {
            throw new Exception("발급 시작일이 종료일보다 늦을 수 없습니다.");
        }

        if (couponMasterDto.getUseStartDate().isAfter(couponMasterDto.getUseEndDate())) {
            throw new Exception("사용 시작일이 종료일보다 늦을 수 없습니다.");
        }
    }

    /**
     * 고유한 쿠폰 코드 생성
     */
    private String generateUniqueCouponCode() {
        String couponCode;
        do {
            couponCode = "CP" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase();
        } while (couponDao.checkCouponCodeExists(couponCode) > 0);

        return couponCode;
    }

    /**
     * 가격 포맷팅
     */
    private String formatPrice(Integer price) {
        if (price == null) return "0";
        return String.format("%,d", price);
    }
}