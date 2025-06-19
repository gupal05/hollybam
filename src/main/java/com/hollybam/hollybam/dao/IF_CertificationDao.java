package com.hollybam.hollybam.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface IF_CertificationDao {
    public int existsByUuid(@Param("uuid") String uuid);
    public void insertGuestUser(Map<String, Object> params);
    Integer selectGuestCodeByUuid(@Param("guestUuid") String guestUuid);
}
