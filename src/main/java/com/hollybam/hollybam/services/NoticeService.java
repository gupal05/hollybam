package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_NoticeDao;
import com.hollybam.hollybam.dto.NoticeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 공지사항 서비스
 * 관리자만 작성 가능한 공지사항 관련 비즈니스 로직 처리
 */
@Service
public class NoticeService {

    @Autowired
    private IF_NoticeDao noticeDao;

    /**
     * 공지사항 등록 (관리자만 가능)
     * @param noticeDto 공지사항 정보
     * @param memberCode 작성자 회원 코드
     * @return 등록 성공 여부
     * @throws Exception 관리자가 아닌 경우 예외 발생
     */
    public boolean createNotice(NoticeDto noticeDto, Integer memberCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(memberCode)) {
            throw new Exception("관리자만 공지사항을 작성할 수 있습니다.");
        }

        noticeDto.setAdminCode(memberCode);
        return noticeDao.insertNotice(noticeDto) > 0;
    }

    /**
     * 공지사항 수정 (관리자만 가능)
     * @param noticeDto 수정할 공지사항 정보
     * @param memberCode 수정자 회원 코드
     * @return 수정 성공 여부
     * @throws Exception 관리자가 아닌 경우 예외 발생
     */
    public boolean updateNotice(NoticeDto noticeDto, Integer memberCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(memberCode)) {
            throw new Exception("관리자만 공지사항을 수정할 수 있습니다.");
        }

        return noticeDao.updateNotice(noticeDto) > 0;
    }

    /**
     * 공지사항 삭제 (관리자만 가능)
     * @param noticeCode 삭제할 공지사항 코드
     * @param memberCode 삭제자 회원 코드
     * @return 삭제 성공 여부
     * @throws Exception 관리자가 아닌 경우 예외 발생
     */
    public boolean deleteNotice(Integer noticeCode, Integer memberCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(memberCode)) {
            throw new Exception("관리자만 공지사항을 삭제할 수 있습니다.");
        }

        return noticeDao.deleteNotice(noticeCode) > 0;
    }

    /**
     * 공지사항 활성화/비활성화 토글 (관리자만 가능)
     * @param noticeCode 공지사항 코드
     * @param isActive 활성화 여부
     * @param memberCode 수정자 회원 코드
     * @return 수정 성공 여부
     * @throws Exception 관리자가 아닌 경우 예외 발생
     */
    public boolean toggleNoticeActive(Integer noticeCode, Boolean isActive, Integer memberCode) throws Exception {
        // 관리자 권한 확인
        if (!isAdminUser(memberCode)) {
            throw new Exception("관리자만 공지사항 상태를 변경할 수 있습니다.");
        }

        return noticeDao.toggleNoticeActive(noticeCode, isActive) > 0;
    }

    /**
     * 공지사항 상세 조회
     * @param noticeCode 공지사항 코드
     * @return 공지사항 상세 정보
     */
    public NoticeDto getNoticeDetail(Integer noticeCode) {
        return noticeDao.selectNoticeDetail(noticeCode);
    }

    /**
     * 활성화된 공지사항 목록 조회 (사용자용)
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 공지사항 목록
     */
    public List<NoticeDto> getActiveNoticeList(int page, int size) {
        int offset = (page - 1) * size;
        return noticeDao.selectActiveNoticeList(offset, size);
    }

    /**
     * 모든 공지사항 목록 조회 (관리자용)
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @return 공지사항 목록
     */
    public List<NoticeDto> getAllNoticeList(int page, int size) {
        int offset = (page - 1) * size;
        return noticeDao.selectAllNoticeList(offset, size);
    }

    /**
     * 중요공지 목록 조회
     * @return 중요공지 목록 (최대 5개)
     */
    public List<NoticeDto> getImportantNoticeList() {
        return noticeDao.selectImportantNoticeList();
    }

    /**
     * 활성화된 공지사항 총 개수 조회 (사용자용)
     * @return 공지사항 총 개수
     */
    public int getActiveNoticeCount() {
        return noticeDao.selectActiveNoticeCount();
    }

    /**
     * 모든 공지사항 총 개수 조회 (관리자용)
     * @return 공지사항 총 개수
     */
    public int getAllNoticeCount() {
        return noticeDao.selectAllNoticeCount();
    }

    /**
     * 관리자 권한 확인
     * @param memberCode 회원 코드
     * @return 관리자 여부
     */
    public boolean isAdminUser(Integer memberCode) {
        if (memberCode == null) {
            return false;
        }
        return noticeDao.isAdmin(memberCode) > 0;
    }

    /**
     * 페이지네이션 정보 계산
     * @param totalCount 총 개수
     * @param page 현재 페이지
     * @param size 페이지 크기
     * @return 총 페이지 수
     */
    public int calculateTotalPages(int totalCount, int page, int size) {
        return (int) Math.ceil((double) totalCount / size);
    }
}