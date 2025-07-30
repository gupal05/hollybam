package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.InquiryDto;

import java.util.List;

public interface IF_InquiryService {
    public List<InquiryDto> selectInquiryList(int memberCode);
    public List<InquiryDto> selectInquiryListForGuest(int guestCode);
    public int insertInquiry(InquiryDto inquiryDto);
    public int insertInquiryForGuest(InquiryDto inquiryDto);
}
