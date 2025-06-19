package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentResponseDto {
    private boolean success;
    private String message;
    private String orderId;
    private String impUid;
    private int amount;
    private String errorCode;
    private String errorMsg;
}
