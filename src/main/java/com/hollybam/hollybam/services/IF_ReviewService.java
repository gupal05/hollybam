package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.BestReviewDto;
import com.hollybam.hollybam.dto.ReviewDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IF_ReviewService {
    boolean checkReviewEligibility(Integer orderItemCode, Integer memCode, Integer guestCode);
    void writeReview(ReviewDto reviewDto, List<MultipartFile> imageFiles) throws IOException;
    int isWroteReview(int orderItemCode);

    /**
     * 전체 베스트 리뷰 조회 (좋아요 수 기준)
     * @return 베스트 리뷰 목록 (최대 12개)
     */
    List<BestReviewDto> selectBestReviews();

    /**
     * ⭐ 새로 추가: 전체 베스트 리뷰 조회 (사용자별 좋아요 상태 포함) - 기존 메서드는 그대로 유지
     * @param memCode 회원 코드 (회원인 경우)
     * @param guestCode 비회원 코드 (비회원인 경우)
     * @return 베스트 리뷰 목록 (최대 12개, 사용자별 좋아요 상태 포함)
     */
    List<BestReviewDto> selectBestReviewsWithLikeStatus(@Param("memCode") Integer memCode,
                                                        @Param("guestCode") Integer guestCode);

    /**
     * 특정 상품의 베스트 리뷰 조회
     * @param productCode 상품 코드
     * @return 해당 상품의 베스트 리뷰 목록 (최대 12개)
     */
    List<BestReviewDto> selectBestReviewsByProduct(@Param("productCode") int productCode);

    /**
     * 회원의 리뷰 통계 조회
     * @param memberCode 회원 코드
     * @return Map - totalReviews, photoReviews, textReviews
     */
    Map<String, Object> selectMemberReviewStats(int memberCode);

    /**
     * 비회원의 리뷰 통계 조회
     * @param guestCode 비회원 코드
     * @return Map - totalReviews, photoReviews, textReviews
     */
    Map<String, Object> selectGuestReviewStats(int guestCode);

    // ====== 기존 메서드들 (하위 호환성 유지) ======
    /**
     * 포토리뷰 최신순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, reviewImage, likeCount
     */
    List<Map<String, Object>> getPhotoReviewDesc();

    /**
     * 포토리뷰 평점순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, reviewImage, likeCount
     */
    List<Map<String, Object>> getPhotoReviewRating();

    /**
     * 포토리뷰 좋아요순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, reviewImage, likeCount
     */
    List<Map<String, Object>> getPhotoReviewLike();

    /**
     * 리뷰 카운트 조회
     * @return Map - photoReviews, textReviews
     */
    Map<String, Object> getReviewCount();

    // ====== 새로 추가된 메서드들 ======
    /**
     * 텍스트리뷰 최신순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, likeCount
     */
    List<Map<String, Object>> getTextReviewDesc();

    /**
     * 텍스트리뷰 평점순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, reviewImage, likeCount
     */
    List<Map<String, Object>> getTextReviewRating();

    /**
     * 텍스트리뷰 좋아요순 조회
     * @return Map - reviewCode, memberCode, guestCode, rating, content, reviewDate, writerName, productName, productImage, likeCount
     */
    List<Map<String, Object>> getTextReviewLike();

    /**
     * 포토리뷰 조회 (페이지네이션 + 필터링 + 좋아요 상태)
     * @param sort 정렬 방식 (latest, rating, likes)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param rating 평점 필터
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 리뷰 목록
     */
    List<Map<String, Object>> getPhotoReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode);

    /**
     * 텍스트리뷰 조회 (페이지네이션 + 필터링 + 좋아요 상태)
     * @param sort 정렬 방식 (latest, rating, likes)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param rating 평점 필터
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 리뷰 목록
     */
    List<Map<String, Object>> getTextReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode);

    /**
     * 리뷰 카운트 조회 (필터링 적용)
     * @param rating 평점 필터 (null이면 전체)
     * @return Map - photoReviews, textReviews
     */
    Map<String, Object> getReviewCount(Integer rating);

    /**
     * 리뷰 좋아요 토글
     * @param reviewCode 리뷰 코드
     * @param memCode 회원 코드 (회원인 경우)
     * @param guestCode 비회원 코드 (비회원인 경우)
     * @return 좋아요 여부 (true: 좋아요 추가, false: 좋아요 취소)
     */
    boolean toggleReviewLike(int reviewCode, Integer memCode, Integer guestCode);

    /**
     * 사용자의 좋아요 상태 확인
     * @param reviewCode 리뷰 코드
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 좋아요 여부
     */
    boolean checkUserLikeStatus(int reviewCode, Integer memCode, Integer guestCode);

    /**
     * 사용자가 좋아요한 리뷰 목록 조회
     * @param reviewCodes 리뷰 코드 목록
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 좋아요한 리뷰 코드 목록
     */
    List<Integer> getUserLikedReviews(List<Integer> reviewCodes, Integer memCode, Integer guestCode);

    /**
     * 내 포토리뷰 조회 (페이지네이션 + 필터링 + 좋아요 상태)
     * @param sort 정렬 방식 (latest, rating, likes)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param rating 평점 필터
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 내 포토리뷰 목록
     */
    List<Map<String, Object>> getMyPhotoReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode);

    /**
     * 내 텍스트리뷰 조회 (페이지네이션 + 필터링 + 좋아요 상태)
     * @param sort 정렬 방식 (latest, rating, likes)
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param rating 평점 필터
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 내 텍스트리뷰 목록
     */
    List<Map<String, Object>> getMyTextReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode);

    /**
     * 내 리뷰 카운트 조회 (필터링 적용)
     * @param rating 평점 필터 (null이면 전체)
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return Map - photoReviews, textReviews
     */
    Map<String, Object> getMyReviewCount(Integer rating, Integer memCode, Integer guestCode);

    /**
     * 내 리뷰 삭제 (본인 확인 후)
     * @param reviewCode 리뷰 코드
     * @param memCode 회원 코드
     * @param guestCode 비회원 코드
     * @return 삭제 성공 여부
     */
    boolean deleteMyReview(int reviewCode, Integer memCode, Integer guestCode);
}