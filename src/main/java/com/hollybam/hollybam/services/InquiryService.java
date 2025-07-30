package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_InquiryDao;
import com.hollybam.hollybam.dto.InquiryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InquiryService implements IF_InquiryService{
    @Autowired
    private IF_InquiryDao inquiryDao;

    @Override
    public List<InquiryDto> selectInquiryList(int memberCode){
        return inquiryDao.selectInquiryList(memberCode);
    }

    @Override
    public List<InquiryDto> selectInquiryListForGuest(int guestCode){
        return inquiryDao.selectInquiryListForGuest(guestCode);
    }

    @Override
    @Transactional
    public int insertInquiry(InquiryDto inquiryDto){
        return inquiryDao.insertInquiry(inquiryDto);
    }

    @Override
    @Transactional
    public int insertInquiryForGuest(InquiryDto inquiryDto){
        return inquiryDao.insertInquiryForGuest(inquiryDto);
    }
}
