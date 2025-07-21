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

    // 목록 조회용 임시 필드들 추가
    private String titleImageUrl;    // title 이미지 URL로 변경
    private Integer sellingPrice;    // 판매가
    private Integer originalPrice;   // 정가

    private List<PriceDto> priceDtoList;
    private List<ImageDto> imageDtoList;
    private List<MovementDto> movementDtoList;
    private List<ProductOptionDto> productOptionDtoList;

    private PriceDto priceDto;        // 단일 가격 정보
    private ImageDto imageDto;        // 대표 이미지
    private ProductOptionDto productOptionDto; // 대표 옵션

    private double reviewAvg;
    private int reviewCount;
    private int wishCount;
}
