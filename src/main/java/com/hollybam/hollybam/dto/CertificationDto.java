package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CertificationDto {
    private boolean adult;
    private String memberName;
    private String memberPhone;
    private LocalDate memberBirth;
    private String gender; // ⬅ 추가
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public CertificationDto(boolean adult, String name, String phone, LocalDate birth, String gender) {
        this.adult = adult;
        this.memberName = name;
        this.memberPhone = phone;
        this.memberBirth = birth;
        this.gender = gender;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }
}


