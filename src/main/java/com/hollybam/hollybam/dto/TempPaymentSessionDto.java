package com.hollybam.hollybam.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TempPaymentSessionDto {
    private String sessionId;
    private String orderData;
    private String memberData;
    private String guestData;
    private String userAgent;
    private String clientIp;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
