package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_SearchDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService implements IF_SearchService {
    @Autowired
    private IF_SearchDao searchDao;

    @Override
    public int countByKeyword(String keyword){
        return searchDao.countByKeyword(keyword);
    }

    @Override
    public List<Map<String,Object>> searchProductsByPage(Map<String,Object> params){
        return searchDao.searchProductsByPage(params);
    }
}
