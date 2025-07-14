package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_ReviewDao;
import com.hollybam.hollybam.dto.BestReviewDto;
import com.hollybam.hollybam.dto.ReviewDto;
import com.hollybam.hollybam.dto.ReviewImageDto;
import com.hollybam.hollybam.util.S3Uploader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReviewService implements IF_ReviewService {

    @Autowired
    private IF_ReviewDao reviewDao;

    @Autowired
    private S3Uploader s3Uploader;

    // ====== 기존 메서드들 ======
    @Override
    public boolean checkReviewEligibility(Integer orderItemCode, Integer memCode, Integer guestCode) {
        try {
            String orderStatus = reviewDao.getOrderStatusByOrderItemCode(orderItemCode);

            if (!"DELIVERED".equals(orderStatus)) {
                log.warn("주문 상태가 배송완료가 아님: {}", orderStatus);
                return false;
            }

            int existingReviewCount = reviewDao.countExistingReview(orderItemCode, memCode, guestCode);
            if (existingReviewCount > 0) {
                log.warn("이미 리뷰가 작성됨. orderItemCode: {}", orderItemCode);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("리뷰 작성 가능 여부 확인 중 오류 발생", e);
            return false;
        }
    }

    @Override
    @Transactional
    public void writeReview(ReviewDto reviewDto, List<MultipartFile> imageFiles) throws IOException {
        try {
            reviewDao.insertReview(reviewDto);
            log.info("리뷰 저장 완료. reviewCode: {}", reviewDto.getReviewCode());

            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        String imageUrl = s3Uploader.upload(imageFile, "review");

                        ReviewImageDto reviewImageDto = new ReviewImageDto();
                        reviewImageDto.setReviewCode(reviewDto.getReviewCode());
                        reviewImageDto.setImageUrl(imageUrl);

                        reviewDao.insertReviewImage(reviewImageDto);
                        log.info("리뷰 이미지 저장 완료. imageUrl: {}", imageUrl);
                    }
                }
            }
        } catch (Exception e) {
            log.error("리뷰 작성 중 오류 발생", e);
            throw e;
        }
    }

    @Override
    public int isWroteReview(int orderItemCode) {
        return reviewDao.isWroteReview(orderItemCode);
    }

    // ====== 베스트 리뷰 관련 메서드들 ======
    @Override
    public List<BestReviewDto> selectBestReviews() {
        return reviewDao.selectBestReviews();
    }

    /**
     * ⭐ 새로 추가: 베스트 리뷰 조회 (사용자별 좋아요 상태 포함) - 기존 메서드는 그대로 유지
     */
    @Override
    public List<BestReviewDto> selectBestReviewsWithLikeStatus(Integer memCode, Integer guestCode) {
        try {
            log.info("베스트 리뷰 조회 (좋아요 상태 포함) - memCode: {}, guestCode: {}", memCode, guestCode);
            return reviewDao.selectBestReviewsWithLikeStatus(memCode, guestCode);
        } catch (Exception e) {
            log.error("베스트 리뷰 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<BestReviewDto> selectBestReviewsByProduct(int productCode) {
        return reviewDao.selectBestReviewsByProduct(productCode);
    }

    // ====== 통계 관련 메서드들 ======
    @Override
    public Map<String, Object> selectMemberReviewStats(int memberCode) {
        return reviewDao.selectMemberReviewStats(memberCode);
    }

    @Override
    public Map<String, Object> selectGuestReviewStats(int guestCode) {
        return reviewDao.selectGuestReviewStats(guestCode);
    }

    // ====== 기존 리뷰 조회 메서드들 (하위 호환성 유지) ======
    @Override
    public List<Map<String, Object>> getPhotoReviewDesc() {
        return reviewDao.getPhotoReviewDesc();
    }

    @Override
    public List<Map<String, Object>> getPhotoReviewRating() {
        return reviewDao.getPhotoReviewRating();
    }

    @Override
    public List<Map<String, Object>> getPhotoReviewLike() {
        return reviewDao.getPhotoReviewLike();
    }

    @Override
    public List<Map<String, Object>> getTextReviewDesc() {
        return reviewDao.getTextReviewDesc();
    }

    @Override
    public List<Map<String, Object>> getTextReviewRating() {
        return reviewDao.getTextReviewRating();
    }

    @Override
    public List<Map<String, Object>> getTextReviewLike() {
        return reviewDao.getTextReviewLike();
    }

    @Override
    public Map<String, Object> getReviewCount() {
        return reviewDao.getReviewCount();
    }

    // ====== 페이지네이션 + 필터링 + 좋아요 상태 메서드들 ======
    @Override
    public List<Map<String, Object>> getPhotoReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode) {
        int offset = (page - 1) * size;

        switch (sort) {
            case "rating":
                return reviewDao.getPhotoReviewRatingWithPaging(offset, size, rating, memCode, guestCode);
            case "likes":
                return reviewDao.getPhotoReviewLikeWithPaging(offset, size, rating, memCode, guestCode);
            case "latest":
            default:
                return reviewDao.getPhotoReviewDescWithPaging(offset, size, rating, memCode, guestCode);
        }
    }

    @Override
    public List<Map<String, Object>> getTextReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode) {
        int offset = (page - 1) * size;

        switch (sort) {
            case "rating":
                return reviewDao.getTextReviewRatingWithPaging(offset, size, rating, memCode, guestCode);
            case "likes":
                return reviewDao.getTextReviewLikeWithPaging(offset, size, rating, memCode, guestCode);
            case "latest":
            default:
                return reviewDao.getTextReviewDescWithPaging(offset, size, rating, memCode, guestCode);
        }
    }

    @Override
    public Map<String, Object> getReviewCount(Integer rating) {
        return reviewDao.getReviewCountWithFilter(rating);
    }

    // ====== 좋아요 관련 메서드들 ======
    @Override
    @Transactional
    public boolean toggleReviewLike(int reviewCode, Integer memCode, Integer guestCode) {
        try {
            // 현재 좋아요 상태 확인
            int likeStatus = reviewDao.checkUserLikeStatus(reviewCode, memCode, guestCode);

            if (likeStatus > 0) {
                // 이미 좋아요 상태면 취소
                reviewDao.deleteReviewLike(reviewCode, memCode, guestCode);
                return false;
            } else {
                // 좋아요 상태가 아니면 추가
                reviewDao.insertReviewLike(reviewCode, memCode, guestCode);
                return true;
            }
        } catch (Exception e) {
            log.error("리뷰 좋아요 토글 중 오류 발생", e);
            throw new RuntimeException("좋아요 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public boolean checkUserLikeStatus(int reviewCode, Integer memCode, Integer guestCode) {
        return reviewDao.checkUserLikeStatus(reviewCode, memCode, guestCode) > 0;
    }

    @Override
    public List<Integer> getUserLikedReviews(List<Integer> reviewCodes, Integer memCode, Integer guestCode) {
        if (reviewCodes == null || reviewCodes.isEmpty()) {
            return new ArrayList<>();
        }
        return reviewDao.getUserLikedReviews(reviewCodes, memCode, guestCode);
    }
}