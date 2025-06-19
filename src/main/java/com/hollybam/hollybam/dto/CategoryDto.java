package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CategoryDto {
    private String categoryCode;  // 컬럼명에 맞게
    private String categoryName;

    private List<CategoryDetailDto> categoryDetailDto;
}

