package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

// 재고 이동 관련
@Getter
@Setter
@ToString
public class MovementDto {
    private int movementId;                 // 재고 이동 코드
    private String movementType;            // 이동 구분
    private int movementQuantity;           // 이동 수량
    private LocalDateTime movementDate;     // 이동 일자
}
