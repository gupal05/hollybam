package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IF_LoginDao {
    public MemberDto getMemberInfo(String memberId);
}
