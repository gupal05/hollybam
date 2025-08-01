package com.hollybam.hollybam.dao.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_AdminPopupDao {
    int insertPopup(Map<String, Object> map);
    List<Map<String, Object>> getPopupList();
    int getPopupCount();
}
