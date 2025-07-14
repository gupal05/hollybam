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
    // 기존 메서드들
    String getOrderStatusByOrderItemCode(int orderItemCode);
    int countExistingReview(@Param("orderItemCode") int orderItemCode,
                            @Param("memCode") Integer memCode,
                            @Param("guestCode") Integer guestCode);
    void insertReview(ReviewDto reviewDto);
    void insertReviewImage(ReviewImageDto reviewImageDto);
    int isWroteReview(int orderItemCode);
    List<BestReviewDto> selectBestReviews();
    List<BestReviewDto> selectBestReviewsByProduct(@Param("productCode") int productCode);
    Map<String, Object> selectMemberReviewStats(int memberCode);
    Map<String, Object> selectGuestReviewStats(int guestCode);

    // ====== 기존 포토리뷰 메서드들 (하위 호환성 유지) ======
    List<Map<String, Object>> getPhotoReviewDesc();
    List<Map<String, Object>> getPhotoReviewRating();
    List<Map<String, Object>> getPhotoReviewLike();
    Map<String, Object> getReviewCount();

    // ====== 새로 추가된 텍스트리뷰 메서드들 ======
    List<Map<String, Object>> getTextReviewDesc();
    List<Map<String, Object>> getTextReviewRating();
    List<Map<String, Object>> getTextReviewLike();

    // ====== 페이지네이션 및 필터링이 적용된 메서드들 ======
    List<Map<String, Object>> getPhotoReviewDescWithPaging(@Param("offset") int offset,
                                                           @Param("limit") int limit,
                                                           @Param("rating") Integer rating,
                                                           @Param("memCode") Integer memCode,
                                                           @Param("guestCode") Integer guestCode);
    List<Map<String, Object>> getPhotoReviewRatingWithPaging(@Param("offset") int offset,
                                                             @Param("limit") int limit,
                                                             @Param("rating") Integer rating,
                                                             @Param("memCode") Integer memCode,
                                                             @Param("guestCode") Integer guestCode);
    List<Map<String, Object>> getPhotoReviewLikeWithPaging(@Param("offset") int offset,
                                                           @Param("limit") int limit,
                                                           @Param("rating") Integer rating,
                                                           @Param("memCode") Integer memCode,
                                                           @Param("guestCode") Integer guestCode);

    List<Map<String, Object>> getTextReviewDescWithPaging(@Param("offset") int offset,
                                                          @Param("limit") int limit,
                                                          @Param("rating") Integer rating,
                                                          @Param("memCode") Integer memCode,
                                                          @Param("guestCode") Integer guestCode);
    List<Map<String, Object>> getTextReviewRatingWithPaging(@Param("offset") int offset,
                                                            @Param("limit") int limit,
                                                            @Param("rating") Integer rating,
                                                            @Param("memCode") Integer memCode,
                                                            @Param("guestCode") Integer guestCode);
    List<Map<String, Object>> getTextReviewLikeWithPaging(@Param("offset") int offset,
                                                          @Param("limit") int limit,
                                                          @Param("rating") Integer rating,
                                                          @Param("memCode") Integer memCode,
                                                          @Param("guestCode") Integer guestCode);

    // 필터링이 적용된 리뷰 카운트 조회
    Map<String, Object> getReviewCountWithFilter(@Param("rating") Integer rating);

    // 좋아요 관련 메서드들
    /**
     * 리뷰 좋아요 추가
     */
    int insertReviewLike(@Param("reviewCode") int reviewCode,
                         @Param("memCode") Integer memCode,
                         @Param("guestCode") Integer guestCode);

    /**
     * 리뷰 좋아요 삭제
     */
    int deleteReviewLike(@Param("reviewCode") int reviewCode,
                         @Param("memCode") Integer memCode,
                         @Param("guestCode") Integer guestCode);

    /**
     * 사용자가 특정 리뷰에 좋아요를 눌렀는지 확인
     */
    int checkUserLikeStatus(@Param("reviewCode") int reviewCode,
                            @Param("memCode") Integer memCode,
                            @Param("guestCode") Integer guestCode);

    /**
     * 사용자가 좋아요한 리뷰 목록 조회
     */
    List<Integer> getUserLikedReviews(@Param("reviewCodes") List<Integer> reviewCodes,
                                      @Param("memCode") Integer memCode,
                                      @Param("guestCode") Integer guestCode);
}