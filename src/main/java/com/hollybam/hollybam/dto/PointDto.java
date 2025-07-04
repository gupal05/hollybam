package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PointDto {
    private int pointCode;
    private int memberCode;
    private int pointChange;
    private String pointType;
    private String description;
    private Integer relatedOrderCode;
    private LocalDateTime createdAt;
}