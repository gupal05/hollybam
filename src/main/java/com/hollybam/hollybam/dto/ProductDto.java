package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class ProductDto {
    private int productCode;            // pk AUTO_INCREMENT
    private String productId;           // 상품 코드 ex) TH-004
    private String productName;         // 상품 이름
    private String productDescription;  // 상품 설명
    private int productOrderCount;      // 상품 누적 판매량
    private int productQuantity;        // 상품 수량 (재고)
    private int productActive;          // 상품 판매 상태
    private LocalDateTime createAt;     // 등록 일자
    private LocalDateTime updateAt;     // 수정 일자

    private List<PriceDto> priceDtoList;
    private List<ImageDto> imageDtoList;
    private List<MovementDto> movementDtoList;
    private List<ProductOptionDto> productOptionDtoList;
}
