package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.DiscountDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

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
}
