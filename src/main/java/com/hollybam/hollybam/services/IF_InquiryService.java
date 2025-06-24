package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.InquiryDto;

import java.util.List;

public interface IF_InquiryService {
    public List<InquiryDto> selectInquiryList(int memberCode);
    public int insertInquiry(InquiryDto inquiryDto);
}
