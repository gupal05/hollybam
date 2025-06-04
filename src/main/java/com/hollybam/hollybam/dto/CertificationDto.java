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
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    public CertificationDto(boolean adult, String name, String phone, LocalDate birth) {
        this.adult = adult;
        this.memberName = name;
        this.memberPhone = phone;
        this.memberBirth = birth;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }


    private LocalDate parseBirthDate(String birthStr) {
        if (birthStr == null || birthStr.length() != 8 || !birthStr.matches("\\d{8}")) {
            throw new IllegalArgumentException("잘못된 생년월일 형식: " + birthStr);
        }

        int year = Integer.parseInt(birthStr.substring(0, 4));
        int month = Integer.parseInt(birthStr.substring(4, 6));
        int day = Integer.parseInt(birthStr.substring(6, 8));

        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("유효하지 않은 날짜: " + birthStr, e);
        }
    }

}

