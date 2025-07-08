package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ReviewLikeDto {
    private Integer likeCode;     // 좋아요 고유 번호 (PK)
    private Integer reviewCode;   // 리뷰 코드 (FK)
    private Integer memCode;      // 회원 코드 (nullable)
    private Integer guestCode;    // 비회원 코드 (nullable)
    private LocalDateTime createdAt;
}

