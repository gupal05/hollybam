package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.NoticeDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 공지사항 DAO 인터페이스
 * 관리자만 작성 가능한 공지사항 관련 데이터베이스 처리
 */
@Mapper
public interface IF_NoticeDao {

    /**
     * 공지사항 등록 (관리자만 가능)
     * @param noticeDto 공지사항 정보
     * @return 등록 성공 시 1, 실패 시 0
     */
    int insertNotice(NoticeDto noticeDto);

    /**
     * 공지사항 수정 (관리자만 가능)
     * @param noticeDto 수정할 공지사항 정보
     * @return 수정 성공 시 1, 실패 시 0
     */
    int updateNotice(NoticeDto noticeDto);

    /**
     * 공지사항 삭제 (관리자만 가능)
     * @param noticeCode 삭제할 공지사항 코드
     * @return 삭제 성공 시 1, 실패 시 0
     */
    int deleteNotice(@Param("noticeCode") Integer noticeCode);

    /**
     * 공지사항 활성화/비활성화 토글 (관리자만 가능)
     * @param noticeCode 공지사항 코드
     * @param isActive 활성화 여부
     * @return 수정 성공 시 1, 실패 시 0
     */
    int toggleNoticeActive(@Param("noticeCode") Integer noticeCode, @Param("isActive") Boolean isActive);

    /**
     * 공지사항 상세 조회
     * @param noticeCode 공지사항 코드
     * @return 공지사항 상세 정보
     */
    NoticeDto selectNoticeDetail(@Param("noticeCode") Integer noticeCode);

    /**
     * 활성화된 공지사항 목록 조회 (사용자용)
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return 공지사항 목록
     */
    List<NoticeDto> selectActiveNoticeList(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 모든 공지사항 목록 조회 (관리자용)
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return 공지사항 목록
     */
    List<NoticeDto> selectAllNoticeList(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 중요공지 목록 조회
     * @return 중요공지 목록
     */
    List<NoticeDto> selectImportantNoticeList();

    /**
     * 활성화된 공지사항 총 개수 조회 (사용자용)
     * @return 공지사항 총 개수
     */
    int selectActiveNoticeCount();

    /**
     * 모든 공지사항 총 개수 조회 (관리자용)
     * @return 공지사항 총 개수
     */
    int selectAllNoticeCount();

    /**
     * 관리자 권한 확인
     * @param memberCode 회원 코드
     * @return 관리자면 1, 아니면 0
     */
    int isAdmin(@Param("memberCode") Integer memberCode);
}