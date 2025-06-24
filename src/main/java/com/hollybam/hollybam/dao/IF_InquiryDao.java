package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.InquiryDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IF_InquiryDao {
    public List<InquiryDto> selectInquiryList(int memberCode);
    public int insertInquiry(InquiryDto inquiryDto);
}
