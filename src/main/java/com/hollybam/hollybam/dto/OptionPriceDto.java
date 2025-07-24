package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class OptionPriceDto {
    private int optionPriceCode;
    private int optionPriceCost;
    private int optionPriceMargin;
    private LocalDateTime createAt;
}
