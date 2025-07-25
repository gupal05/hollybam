package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ProductOptionDto {
    private int optionCode;
    private String optionName;
    private String optionValue;
    private int optionPrice;
    private int optionQuantity;

    private List<OptionPriceDto> optionPriceDtoList;
    private OptionPriceDto optionPriceDto;
    private int optionCost;

    private Integer productCode;
}
