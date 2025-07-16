// src/main/java/com/hollybam/hollybam/dao/IF_PointDao.java
package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.PointDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IF_PointDao {

    /**
     * 회원의 현재 적립금 조회 (모든 적립금 변동 내역 합계)
     * @param memberCode 회원 코드
     * @return 현재 적립금 잔액
     */
    Integer getCurrentPoints(@Param("memberCode") int memberCode);

    /**
     * 적립금 변동 로그 삽입
     * @param pointDto 적립금 변동 정보
     * @return 삽입된 행 수
     */
    int insertPointLog(PointDto pointDto);
}