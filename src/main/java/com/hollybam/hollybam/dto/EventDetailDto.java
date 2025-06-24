package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class EventDetailDto {
    private Integer eventDetailCode;        // 상세 고유 코드
    private Integer eventCode;              // 어떤 이벤트에 속한 상세인지
    private String eventDetailContentImg;   // 이미지 URL
    private String eventDetailActionUrl;    // 클릭 시 이동할 URL (nullable)
    private LocalDateTime createdAt;        // 등록일
}
