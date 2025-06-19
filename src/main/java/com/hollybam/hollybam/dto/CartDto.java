package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CartDto {
    private int cartCode;
    private Integer memCode;          // int → Integer로 변경 (null 허용)
    private Integer guestCode;        // int → Integer로 변경 (null 허용)
    private int productCode;
    private Integer optionCode;       // int → Integer로 변경 (옵션 없으면 null)
    private int quantity;
    private boolean selected;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    // 조인용 필드들
    private ProductDto productDto;
    private ProductOptionDto productOptionDto;
    private PriceDto priceDto;
    private ImageDto imageDto;
}
