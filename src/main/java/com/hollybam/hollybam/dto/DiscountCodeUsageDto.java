package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class DiscountCodeUsageDto {
    private Integer usageCode;          // usage_code - 고유 ID (PK)
    private Integer discountCode;       // discount_code - 어떤 할인코드인지 (FK)
    private Integer memCode;            // mem_code - 사용한 회원 (FK)
    private Integer guestCode;
    private Integer orderCode;
    private Integer discountAmount;
    private LocalDateTime usedAt;       // used_at - 사용 시각

    // 조인용 필드 (필요시 사용)
    private DiscountDto discountDto;    // 할인코드 정보
    private MemberDto memberDto;        // 회원 정보
}