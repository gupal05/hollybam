package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
public class MemberDto {
    private int memberCode;
    private String memberName;
    private String memberId;
    private String memberPw;
    private String memberAddr;
    private String memberPhone;
    private String memberZip;
    private String memberMail;
    private LocalDate memberBirth;
    private String memberGender;
    private String memberRole;
    private String memberLoginType;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private String di;

    private boolean isAdultVerified;
    private LocalDateTime adultVerifiedAt;
    private boolean isDeleted;
    private LocalDateTime deletedAt;
}
