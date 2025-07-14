package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.BestReviewDto;
import com.hollybam.hollybam.dto.ReviewDto;
import com.hollybam.hollybam.dto.ReviewImageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_ReviewDao {
    String getOrderStatusByOrderItemCode(Integer orderItemCode);
    int countExistingReview(Integer orderItemCode, Integer memCode, Integer guestCode);
    void insertReview(ReviewDto reviewDto);
    void insertReviewImage(ReviewImageDto imageDto);
    int isWroteReview(int orderItemCode);
    /**
     * 전체 베스트 리뷰 조회 (좋아요 수 기준)
     * @return 베스트 리뷰 목록 (최대 12개)
     */
    List<BestReviewDto> selectBestReviews();

    /**
     * 특정 상품의 베스트 리뷰 조회
     * @param productCode 상품 코드
     * @return 해당 상품의 베스트 리뷰 목록 (최대 12개)
     */
    List<BestReviewDto> selectBestReviewsByProduct(@Param("productCode") int productCode);

    /**
     * 회원의 리뷰 통계 조회
     * 회원 코드
     * @return Map - totalReviews, photoReviews, textReviews
     */
    Map<String, Object> selectMemberReviewStats(int memberCode);

    /**
     * 비회원의 리뷰 통계 조회
     * guestCode 비회원 코드
     * @return Map - totalReviews, photoReviews, textReviews
     */
    Map<String, Object> selectGuestReviewStats(int guestCode);

    /**
     * 리뷰 최신순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, reviewImage, likeCount
     */
    List<Map<String, Object>> getPhotoReviewDesc();

    /**
     * 리뷰 평점순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, reviewImage, likeCount
     */
    List<Map<String, Object>> getPhotoReviewRating();

    /**
     * 리뷰 좋아요순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, reviewImage, likeCount
     */
    List<Map<String, Object>> getPhotoReviewLike();

    /**
     * 비회원의 리뷰 통계 조회
     * guestCode 비회원 코드
     * @return Map - photoReviews, textReviews
     */
    Map<String, Object> getReviewCount();

}
