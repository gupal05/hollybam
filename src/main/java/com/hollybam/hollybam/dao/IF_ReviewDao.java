package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.BestReviewDto;
import com.hollybam.hollybam.dto.ReviewDetailDto;
import com.hollybam.hollybam.dto.ReviewDto;
import com.hollybam.hollybam.dto.ReviewImageDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface IF_ReviewDao {

    // ====== 기존 메서드들 ======
    String getOrderStatusByOrderItemCode(@Param("orderItemCode") Integer orderItemCode);
    int countExistingReview(@Param("orderItemCode") Integer orderItemCode,
                            @Param("memCode") Integer memCode,
                            @Param("guestCode") Integer guestCode);
    void insertReview(ReviewDto reviewDto);
    void insertReviewImage(ReviewImageDto reviewImageDto);
    int isWroteReview(int orderItemCode);

    // ====== 베스트 리뷰 관련 메서드들 ======
    List<BestReviewDto> selectBestReviews();

    /**
     * ⭐ 새로 추가: 베스트 리뷰 조회 (사용자별 좋아요 상태 포함) - 기존 메서드는 그대로 유지
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 베스트 리뷰 목록 (사용자별 좋아요 상태 포함)
     */
    List<BestReviewDto> selectBestReviewsWithLikeStatus(@Param("memCode") Integer memCode,
                                                        @Param("guestCode") Integer guestCode);

    List<BestReviewDto> selectBestReviewsByProduct(@Param("productCode") int productCode);

    // ====== 회원/비회원 통계 ======
    Map<String, Object> selectMemberReviewStats(@Param("memberCode") int memberCode);
    Map<String, Object> selectGuestReviewStats(@Param("guestCode") int guestCode);

    // ====== 기존 리뷰 조회 메서드들 (하위 호환성) ======
    List<Map<String, Object>> getPhotoReviewDesc();
    List<Map<String, Object>> getPhotoReviewRating();
    List<Map<String, Object>> getPhotoReviewLike();
    List<Map<String, Object>> getTextReviewDesc();
    List<Map<String, Object>> getTextReviewRating();
    List<Map<String, Object>> getTextReviewLike();
    Map<String, Object> getReviewCount();

    // ====== 페이지네이션 + 필터링 + 좋아요 상태 메서드들 ======
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

    /**
     * 내 포토리뷰 조회 (페이지네이션 + 필터링 + 좋아요 상태)
     */
    List<Map<String, Object>> getMyPhotoReviews(@Param("sort") String sort,
                                                @Param("offset") int offset,
                                                @Param("limit") int limit,
                                                @Param("rating") Integer rating,
                                                @Param("memCode") Integer memCode,
                                                @Param("guestCode") Integer guestCode);

    /**
     * 내 텍스트리뷰 조회 (페이지네이션 + 필터링 + 좋아요 상태)
     */
    List<Map<String, Object>> getMyTextReviews(@Param("sort") String sort,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit,
                                               @Param("rating") Integer rating,
                                               @Param("memCode") Integer memCode,
                                               @Param("guestCode") Integer guestCode);

    /**
     * 내 리뷰 카운트 조회 (필터링 적용)
     */
    Map<String, Object> getMyReviewCount(@Param("rating") Integer rating,
                                         @Param("memCode") Integer memCode,
                                         @Param("guestCode") Integer guestCode);

    /**
     * 본인 리뷰인지 확인
     */
    boolean checkIsMyReview(@Param("reviewCode") int reviewCode,
                            @Param("memCode") Integer memCode,
                            @Param("guestCode") Integer guestCode);

    /**
     * 리뷰 비활성화 (삭제 대신)
     */
    int deactivateReview(@Param("reviewCode") int reviewCode);

    /**
     * 리뷰 상세 조회 (모든 관련 정보 포함)
     * @param reviewCode 리뷰 코드
     * @return 리뷰 상세 정보
     */
    ReviewDetailDto selectReviewDetail(@Param("reviewCode") int reviewCode);

    /**
     * 리뷰 이미지 목록 조회
     * @param reviewCode 리뷰 코드
     * @return 리뷰 이미지 목록
     */
    List<ReviewImageDto> selectReviewImages(@Param("reviewCode") int reviewCode);

    /**
     * 리뷰 좋아요 수 조회
     * @param reviewCode 리뷰 코드
     * @return 좋아요 수
     */
    int selectReviewLikeCount(@Param("reviewCode") int reviewCode);

    /**
     * 사용자의 리뷰 좋아요 여부 조회
     * @param reviewCode 리뷰 코드
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 좋아요 여부 (1: 좋아요, 0: 안함)
     */
    int selectUserReviewLikeStatus(@Param("reviewCode") int reviewCode,
                                   @Param("memCode") Integer memCode,
                                   @Param("guestCode") Integer guestCode);

    /**
     * 같은 상품의 관련 리뷰 조회
     * @param productCode 상품 코드
     * @param currentReviewCode 현재 리뷰 코드 (제외)
     * @param limit 조회 개수
     * @return 관련 리뷰 목록
     */
    List<Map<String, Object>> selectRelatedReviews(@Param("productCode") int productCode,
                                                   @Param("currentReviewCode") int currentReviewCode,
                                                   @Param("limit") int limit);

    /**
     * 리뷰 작성자 확인
     * @param reviewCode 리뷰 코드
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 일치 여부 (1: 일치, 0: 불일치)
     */
    int checkReviewOwnership(@Param("reviewCode") int reviewCode,
                             @Param("memCode") Integer memCode,
                             @Param("guestCode") Integer guestCode);

    /**
     * 리뷰 내용 및 평점 수정
     * @param reviewCode 리뷰 코드
     * @param content 수정할 내용
     * @param rating 수정할 평점
     * @return 수정된 행 수
     */
    int updateReviewContent(@Param("reviewCode") int reviewCode,
                            @Param("content") String content,
                            @Param("rating") int rating);

    /**
     * 리뷰 논리 삭제 (is_active = 0)
     * @param reviewCode 리뷰 코드
     * @return 삭제된 행 수
     */
    int deleteReviewLogical(@Param("reviewCode") int reviewCode);

    /**
     * 리뷰 이미지 삭제
     * @param imageIds 삭제할 이미지 ID 목록
     * @return 삭제된 행 수
     */
    int deleteReviewImages(@Param("imageIds") List<Integer> imageIds);
}