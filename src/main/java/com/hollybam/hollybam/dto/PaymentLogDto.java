package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PaymentLogDto {
    // PK
    private int logCode;
    // PG 거래 ID
    private String tid;
    //결제 방식
    private String payMethod;
    // 결제(승인)된 금액
    private int amount;
    // PG 응답 코드
    private String resultCode;
    // PG 응답 메세지
    private String resultMsg;
    // 생성 시간
    private LocalDateTime createAt;
    // 수정 시간
    private LocalDateTime updateAt;
    
    // 조인용
    private int orderCode;
    private int memberCode;
    private int guestCode;
}
