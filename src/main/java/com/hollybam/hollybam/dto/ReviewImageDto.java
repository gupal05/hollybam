package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ReviewImageDto {
    private Integer reviewImageCode; // 리뷰 이미지 고유 번호 (PK)
    private Integer reviewCode;      // 연결된 리뷰 코드 (FK)
    private String imageUrl;         // S3 또는 서버 URL
    private Integer imageOrder;      // 정렬 순서
    private LocalDateTime createdAt;
}
