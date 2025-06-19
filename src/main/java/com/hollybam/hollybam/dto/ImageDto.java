package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ImageDto {
    private int imageCode;      // 이미지 코드
    private String imageUrl;    // 이미지 경로
    private String imageType;   // 이미지 타입 (title, thumbnail, content, webp)
    private int imageOrder;     // 이미지 순서
    private String imageWebp;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
