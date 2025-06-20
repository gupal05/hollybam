package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface IF_SignupDao {
    public int dupCheckId(String memberId);
    public int signup(MemberDto memberDto);
    public MemberDto getGuestInfo(String uuId);
    public void deleteGuestByUuid(String uuid);
}
