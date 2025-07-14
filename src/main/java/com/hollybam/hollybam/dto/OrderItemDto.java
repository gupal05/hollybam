package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class OrderItemDto {
    private int orderItemCode;
    private int orderCode;
    private int productCode;
    private Integer optionCode;
    private int quantity;
    private int unitPrice;
    private int optionPrice;
    private int totalPrice;
    private LocalDateTime createAt;

    // 조인용 필드
    private ProductDto productDto;
    private ProductOptionDto productOptionDto;
    private ReviewDto reviewDto;
}
