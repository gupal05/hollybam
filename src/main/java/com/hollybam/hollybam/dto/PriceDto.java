package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PriceDto {
    private int priceCode;      // 가격 코드
    private int priceOriginal;  // 정가
    private int priceCost;      // 원가
    private int priceSelling;   // 판매가
    private int priceMargin;    // 이윤
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    private int percentage;
}
