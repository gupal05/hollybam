package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
public class SpecialSaleDto {
    private String specialSaleCode;
    private int specialSalePrice;
    private Date startDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    private int productCode;
}
