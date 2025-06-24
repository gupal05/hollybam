package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class EventDto {
    private Integer eventCode;              // 이벤트 고유 코드
    private String eventTitle;             // 이벤트 제목
    private String eventDescription;       // 이벤트 간략 설명
    private LocalDate eventStart;          // 시작일
    private LocalDate eventEnd;            // 종료일 (nullable)
    private String thumbnailUrl;           // 썸네일 이미지 URL
    private LocalDateTime createdAt;       // 등록일
    private LocalDateTime updatedAt;       // 수정일
    private String eventStatus;            // 이벤트 진행 상태
}
