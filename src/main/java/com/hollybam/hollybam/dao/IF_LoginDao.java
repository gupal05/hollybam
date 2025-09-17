package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface IF_LoginDao {

    /**
     * 로그인 시 회원 정보 조회
     * @param memberId 회원 ID
     * @return MemberDto 회원 정보
     */
    MemberDto getMemberInfo(String memberId);

    /**
     * 네이버 회원 존재 여부 확인
     * @param naverId 네이버 회원 ID
     * @return 존재하면 1 이상, 없으면 0
     */
    int isNaverMember(String naverId);

    /**
     * 구글 회원 존재 여부 확인
     * @param googleId 구글 회원 ID (google_ prefix 포함)
     * @return 존재하면 1 이상, 없으면 0
     */
    int isGoogleMember(String googleId);

    public void upMemberTestPassword(@Param("data") Map<String, Object> data);
}
