package com.hollybam.hollybam.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistDto {
    private int wishlistCode;
    private Integer memCode;      // 회원 코드 (nullable)
    private Integer guestCode;    // 비회원 코드 (nullable)
    private int productCode;      // 상품 코드
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // 조인용 필드들
    private ProductDto productDto;  // 상품 정보
    private String productName;     // 상품명
    private String productId;       // 상품 아이디
    private String productImage;    // 상품 대표 이미지
    private int sellingPrice;       // 판매가
    private int originalPrice;      // 정가
    private boolean productActive;  // 상품 활성화 여부
}