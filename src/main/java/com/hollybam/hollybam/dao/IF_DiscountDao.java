package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.DiscountDto;
import com.hollybam.hollybam.dto.DiscountCodeUsageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IF_DiscountDao {
    int insertDiscount(DiscountDto discountDto);

    /**
     * 할인코드로 할인 정보 조회
     * @param discountId 할인코드
     * @return 할인 정보
     */
    DiscountDto selectDiscountByCode(String discountId) throws Exception;

    /**
     * 활성화된 할인코드 목록 조회
     * @return 활성화된 할인코드 목록
     */
    List<DiscountDto> selectActiveDiscounts() throws Exception;

    // ===== 🆕 할인코드 사용 내역 관련 메서드 추가 =====

    /**
     * 회원의 특정 할인코드 사용 여부 확인
     * @param discountCode 할인코드 번호 (discount 테이블의 PK)
     * @param memCode 회원 코드
     * @return 사용 내역이 있으면 1, 없으면 0
     */
    int checkDiscountCodeUsage(@Param("discountCode") Integer discountCode, @Param("memCode") Integer memCode);

    /**
     * 할인코드 사용 내역 저장
     * @param usageDto 사용 내역 정보
     * @return 저장 결과 (1: 성공, 0: 실패)
     */
    int insertDiscountCodeUsage(DiscountCodeUsageDto usageDto);

    /**
     * 회원의 할인코드 사용 내역 조회
     * @param memCode 회원 코드
     * @return 사용 내역 목록
     */
    List<DiscountCodeUsageDto> selectDiscountUsageByMember(Integer memCode);
}