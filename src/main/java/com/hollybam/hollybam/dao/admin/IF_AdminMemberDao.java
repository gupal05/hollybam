package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminMemberDao {

    /**
     * 관리자용 회원 목록 조회 (검색 조건 포함)
     * @param searchParams 검색 조건 (Map 형태)
     * @return 회원 목록 (Map 형태)
     */
    List<Map<String, Object>> selectMemberList(@Param("params") Map<String, Object> searchParams);

    /**
     * 관리자용 회원 총 개수 조회 (검색 조건 포함)
     * @param searchParams 검색 조건 (Map 형태)
     * @return 회원 총 개수
     */
    int selectMemberCount(@Param("params") Map<String, Object> searchParams);
}