package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductOptionDto {
    private int optionCode;
    private String optionName;
    private String optionValue;
    private int optionPrice;
    private int optionQuantity;
}
