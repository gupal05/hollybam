package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class GuestUserDto {
    private int guestCode;
    private String guestUuid;
    private LocalDate guestBirth;
    private String guestGender;
    private String guestPhone;
    private boolean isAdultVerified;
    private LocalDateTime adultVerifiedAt;
    private LocalDateTime createAt;
}
