package com.hollybam.hollybam.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ReviewDto {
    private Integer reviewCode;         // 리뷰 고유 번호 (PK)
    private Integer productCode;        // 상품 코드 (FK)
    private Integer memCode;            // 회원 코드 (nullable)
    private Integer guestCode;          // 비회원 코드 (nullable)
    private Integer rating;             // 평점 (1~5)
    private String content;             // 리뷰 본문
    private Boolean isActive;           // 공개 여부 (true: 공개, false: 숨김)
    private LocalDateTime createdAt;    // 작성 시각
    private LocalDateTime updatedAt;    // 수정 시각

    private List<ReviewImageDto> imageList; // 첨부 이미지 목록
}
