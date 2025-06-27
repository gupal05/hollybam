package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class GuestDto {
    private int guestCode;
    private String guestDi;
    private String guestName;
    private LocalDate guestBirth;
    private String guestGender;
    private String guestPhone;
    private LocalDate createAt;

    private boolean isAdultVerified;
    private LocalDateTime adultVerifiedAt;
}
