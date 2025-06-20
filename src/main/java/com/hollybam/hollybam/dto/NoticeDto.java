package com.hollybam.hollybam.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 공지사항 DTO
 * 관리자만 작성 가능한 공지사항 정보를 담는 클래스
 */
@Data
public class NoticeDto {

    // 공지사항 기본 정보
    private Integer noticeCode;         // 공지사항 고유 식별자
    private Integer adminCode;          // 작성한 관리자 코드
    private String noticeTitle;         // 공지사항 제목
    private String noticeContent;       // 공지사항 내용

    // 공지사항 설정
    private Boolean isImportant;        // 중요공지 여부 (true: 중요, false: 일반)
    private Boolean isActive;           // 공지 활성화 여부 (true: 활성, false: 비활성)

    // 시간 정보
    private LocalDateTime createdAt;    // 공지 작성 일시
    private LocalDateTime updatedAt;    // 공지 수정 일시

    // 추가 정보 (조인용)
    private String adminName;           // 작성한 관리자 이름
    private String adminId;             // 작성한 관리자 아이디

    // 생성자
    public NoticeDto() {}

    /**
     * 공지사항 생성용 생성자
     */
    public NoticeDto(Integer adminCode, String noticeTitle, String noticeContent) {
        this.adminCode = adminCode;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.isImportant = false;   // 기본값: 일반공지
        this.isActive = true;       // 기본값: 활성화
    }

    /**
     * 중요공지 생성용 생성자
     */
    public NoticeDto(Integer adminCode, String noticeTitle, String noticeContent, Boolean isImportant) {
        this.adminCode = adminCode;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.isImportant = isImportant;
        this.isActive = true;       // 기본값: 활성화
    }
}