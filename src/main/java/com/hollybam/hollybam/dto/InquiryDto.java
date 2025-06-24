package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class InquiryDto {
    private int inquiryCode;
    private String inquiryTitle;
    private String inquiryContent;
    private String inquiryCategory;
    private int inquiryStatus;
    private String inquiryEmail;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
