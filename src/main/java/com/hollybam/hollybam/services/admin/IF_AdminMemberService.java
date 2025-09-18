package com.hollybam.hollybam.services.admin;

import java.util.List;
import java.util.Map;

public interface IF_AdminMemberService {

    /**
     * 관리자용 회원 목록 조회
     * @param searchParams 검색 조건 (Map 형태)
     * @return 회원 목록
     * @throws Exception
     */
    List<Map<String, Object>> getMemberList(Map<String, Object> searchParams) throws Exception;

    /**
     * 관리자용 회원 총 개수 조회
     * @param searchParams 검색 조건 (Map 형태)
     * @return 회원 총 개수
     * @throws Exception
     */
    int getMemberCount(Map<String, Object> searchParams) throws Exception;
}