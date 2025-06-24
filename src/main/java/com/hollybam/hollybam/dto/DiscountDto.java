package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class DiscountDto {
    private Integer discountCode;
    private String discountId;
    private String discountDescription;
    private String discountType;
    private Integer discountValue;
    private Integer minOrderPrice;
    private LocalDate expiredAt;
    private LocalDateTime createdAt;
}
