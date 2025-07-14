package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.ReviewDto;
import com.hollybam.hollybam.dto.ReviewImageDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IF_ReviewDao {
    String getOrderStatusByOrderItemCode(Integer orderItemCode);
    int countExistingReview(Integer orderItemCode, Integer memCode, Integer guestCode);
    void insertReview(ReviewDto reviewDto);
    void insertReviewImage(ReviewImageDto imageDto);
    int isWroteReview(int orderItemCode);
}
