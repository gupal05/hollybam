package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class DeliveryDto {
    private int deliveryId;
    private int orderCode;
    private String courier;
    private String trackingNumber;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private String deliveryStatus; // READY, SHIPPING, DELIVERED, RETURNING, RETURNED
    private String deliveryMemo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}