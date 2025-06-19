package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CategoryDetailDto {
    private String cateDetailCode;
    private String cateDetailName;

    private List<ProductDto> productList;
}
