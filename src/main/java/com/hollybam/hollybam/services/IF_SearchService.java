package com.hollybam.hollybam.services;

import java.util.List;
import java.util.Map;

public interface IF_SearchService {
    int countByKeyword(String keyword);
    List<Map<String,Object>> searchProductsByPage(Map<String,Object> params);
}
