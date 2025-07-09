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
    private int logCode;
    private String orderId;
    private String merchantUid;
    private int amount;
    private String status;
    private String pgProvider;
    private String pgTid;
    private String payMethod;

    // 성인인증 로그
    private boolean adultVerified;
    private LocalDate buyerBirth;
    private String verificationMethod;

    // 응답 데이터
    private String responseData;
    private String errorMsg;

    private LocalDateTime createAt;
}
