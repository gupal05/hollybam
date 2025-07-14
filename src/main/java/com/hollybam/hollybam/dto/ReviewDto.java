package com.hollybam.hollybam.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class ReviewDto {
    private Integer reviewCode;
    private Integer orderItemCode;  // 변경된 포인트
    private Integer memCode;
    private Integer guestCode;
    private Integer rating;
    private String content;
    private Boolean isActive;
    private Boolean isReview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ReviewImageDto> imageList;
}
