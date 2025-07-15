package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_ReviewDao;
import com.hollybam.hollybam.dto.BestReviewDto;
import com.hollybam.hollybam.dto.ReviewDetailDto;
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
import java.util.HashMap;
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
                int idx = 0;
                for (MultipartFile imageFile : imageFiles) {
                    if (!imageFile.isEmpty()) {
                        String imageUrl = s3Uploader.upload(imageFile, "review");

                        ReviewImageDto reviewImageDto = new ReviewImageDto();
                        reviewImageDto.setReviewCode(reviewDto.getReviewCode());
                        reviewImageDto.setImageUrl(imageUrl);
                        reviewImageDto.setImageOrder(idx);
                        reviewDao.insertReviewImage(reviewImageDto);
                        idx++;
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

    @Override
    public List<Map<String, Object>> getMyPhotoReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode) {
        try {
            log.info("내 포토리뷰 조회 - sort: {}, page: {}, size: {}, rating: {}, memCode: {}, guestCode: {}",
                    sort, page, size, rating, memCode, guestCode);

            // page를 offset으로 변환 (1부터 시작하는 페이지를 0부터 시작하는 offset으로)
            int offset = (page - 1) * size;

            List<Map<String, Object>> result = reviewDao.getMyPhotoReviews(sort, offset, size, rating, memCode, guestCode);

            // ⭐ writerName 디버깅 - 여기에 추가하세요
            System.out.println("=== 포토리뷰 writerName 확인 ===");
            for (int i = 0; i < result.size(); i++) {
                Map<String, Object> review = result.get(i);
                System.out.println("리뷰 " + i + ":");
                System.out.println("  - reviewCode: " + review.get("reviewCode"));
                System.out.println("  - writerName: " + review.get("writerName"));
                System.out.println("  - guestWriterName: " + review.get("guestWriterName"));
                System.out.println("  - memberCode: " + review.get("memberCode"));
                System.out.println("  - guestCode: " + review.get("guestCode"));
                System.out.println("  - productName: " + review.get("productName"));
                System.out.println("---");
            }
            System.out.println("=== 포토리뷰 디버깅 끝 ===");

            return result;
        } catch (Exception e) {
            log.error("내 포토리뷰 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getMyTextReviews(String sort, int page, int size, Integer rating, Integer memCode, Integer guestCode) {
        try {
            log.info("내 텍스트리뷰 조회 - sort: {}, page: {}, size: {}, rating: {}, memCode: {}, guestCode: {}",
                    sort, page, size, rating, memCode, guestCode);

            // page를 offset으로 변환 (1부터 시작하는 페이지를 0부터 시작하는 offset으로)
            int offset = (page - 1) * size;

            List<Map<String, Object>> result = reviewDao.getMyTextReviews(sort, offset, size, rating, memCode, guestCode);

            // ⭐ writerName 디버깅 - 여기에 추가하세요
            System.out.println("=== 텍스트리뷰 writerName 확인 ===");
            for (int i = 0; i < result.size(); i++) {
                Map<String, Object> review = result.get(i);
                System.out.println("리뷰 " + i + ":");
                System.out.println("  - reviewCode: " + review.get("reviewCode"));
                System.out.println("  - writerName: " + review.get("writerName"));
                System.out.println("  - guestWriterName: " + review.get("guestWriterName"));
                System.out.println("  - memberCode: " + review.get("memberCode"));
                System.out.println("  - guestCode: " + review.get("guestCode"));
                System.out.println("  - productName: " + review.get("productName"));
                System.out.println("---");
            }
            System.out.println("=== 텍스트리뷰 디버깅 끝 ===");

            return result;
        } catch (Exception e) {
            log.error("내 텍스트리뷰 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getMyReviewCount(Integer rating, Integer memCode, Integer guestCode) {
        try {
            log.info("내 리뷰 카운트 조회 - rating: {}, memCode: {}, guestCode: {}", rating, memCode, guestCode);

            Map<String, Object> result = reviewDao.getMyReviewCount(rating, memCode, guestCode);

            // null 처리
            if (result.get("photoReviews") == null) result.put("photoReviews", 0);
            if (result.get("textReviews") == null) result.put("textReviews", 0);

            return result;
        } catch (Exception e) {
            log.error("내 리뷰 카운트 조회 중 오류 발생", e);
            Map<String, Object> result = new HashMap<>();
            result.put("photoReviews", 0);
            result.put("textReviews", 0);
            return result;
        }
    }

    @Override
    public boolean deleteMyReview(int reviewCode, Integer memCode, Integer guestCode) {
        try {
            log.info("내 리뷰 삭제 - reviewCode: {}, memCode: {}, guestCode: {}", reviewCode, memCode, guestCode);

            // 본인 리뷰인지 확인
            boolean isMyReview = reviewDao.checkIsMyReview(reviewCode, memCode, guestCode);

            if (!isMyReview) {
                log.warn("본인 리뷰가 아님 - reviewCode: {}", reviewCode);
                return false;
            }

            // 리뷰 비활성화 (실제 삭제 대신)
            int updated = reviewDao.deactivateReview(reviewCode);

            if (updated > 0) {
                log.info("리뷰 비활성화 완료 - reviewCode: {}", reviewCode);
                return true;
            } else {
                log.warn("리뷰 비활성화 실패 - reviewCode: {}", reviewCode);
                return false;
            }

        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생 - reviewCode: {}", reviewCode, e);
            return false;
        }
    }

    @Override
    public ReviewDetailDto getReviewDetail(int reviewCode, Integer memCode, Integer guestCode) {
        try {
            log.info("리뷰 상세 조회 - reviewCode: {}, memCode: {}, guestCode: {}", reviewCode, memCode, guestCode);

            // 기본 리뷰 정보 조회
            ReviewDetailDto reviewDetail = reviewDao.selectReviewDetail(reviewCode);
            if (reviewDetail == null) {
                log.warn("리뷰를 찾을 수 없음 - reviewCode: {}", reviewCode);
                return null;
            }

            // 리뷰 이미지 목록 조회
            List<ReviewImageDto> reviewImages = reviewDao.selectReviewImages(reviewCode);
            reviewDetail.setReviewImages(reviewImages);

            // 현재 사용자의 좋아요 상태 확인
            int isLiked = reviewDao.selectUserReviewLikeStatus(reviewCode, memCode, guestCode);
            reviewDetail.setLiked(isLiked > 0);

            // 현재 사용자가 작성자인지 확인 (수정/삭제 권한)
            int isOwner = reviewDao.checkReviewOwnership(reviewCode, memCode, guestCode);
            reviewDetail.setCanEdit(isOwner > 0);
            reviewDetail.setCanDelete(isOwner > 0);

            log.info("리뷰 상세 조회 완료 - reviewCode: {}", reviewCode);
            return reviewDetail;

        } catch (Exception e) {
            log.error("리뷰 상세 조회 중 오류 발생 - reviewCode: {}", reviewCode, e);
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getRelatedReviews(int productCode, int currentReviewCode, int limit) {
        try {
            log.info("관련 리뷰 조회 - productCode: {}, currentReviewCode: {}, limit: {}",
                    productCode, currentReviewCode, limit);

            return reviewDao.selectRelatedReviews(productCode, currentReviewCode, limit);

        } catch (Exception e) {
            log.error("관련 리뷰 조회 중 오류 발생", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean canEditReview(int reviewCode, Integer memCode, Integer guestCode) {
        try {
            int ownershipCount = reviewDao.checkReviewOwnership(reviewCode, memCode, guestCode);
            return ownershipCount > 0;
        } catch (Exception e) {
            log.error("리뷰 수정 권한 확인 중 오류 발생", e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateReview(int reviewCode, String content, int rating,
                                Integer memCode, Integer guestCode,
                                List<MultipartFile> newImages, List<Integer> removeImageIds) throws IOException {
        try {
            log.info("리뷰 수정 시작 - reviewCode: {}", reviewCode);

            // 권한 확인
            if (!canEditReview(reviewCode, memCode, guestCode)) {
                log.warn("리뷰 수정 권한 없음 - reviewCode: {}", reviewCode);
                return false;
            }

            // 리뷰 내용 및 평점 수정
            int updatedRows = reviewDao.updateReviewContent(reviewCode, content, rating);
            if (updatedRows == 0) {
                log.warn("리뷰 수정 실패 - reviewCode: {}", reviewCode);
                return false;
            }

            // 삭제할 이미지가 있다면 삭제
            if (removeImageIds != null && !removeImageIds.isEmpty()) {
                reviewDao.deleteReviewImages(removeImageIds);
                log.info("리뷰 이미지 삭제 완료 - 삭제된 이미지 수: {}", removeImageIds.size());
            }

            // 새 이미지가 있다면 추가
            if (newImages != null && !newImages.isEmpty()) {
                for (MultipartFile imageFile : newImages) {
                    if (!imageFile.isEmpty()) {
                        String imageUrl = s3Uploader.upload(imageFile, "review");

                        ReviewImageDto reviewImageDto = new ReviewImageDto();
                        reviewImageDto.setReviewCode(reviewCode);
                        reviewImageDto.setImageUrl(imageUrl);

                        reviewDao.insertReviewImage(reviewImageDto);
                        log.info("새 리뷰 이미지 추가 완료 - imageUrl: {}", imageUrl);
                    }
                }
            }

            log.info("리뷰 수정 완료 - reviewCode: {}", reviewCode);
            return true;

        } catch (Exception e) {
            log.error("리뷰 수정 중 오류 발생 - reviewCode: {}", reviewCode, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteReview(int reviewCode, Integer memCode, Integer guestCode) {
        try {
            log.info("리뷰 삭제 시작 - reviewCode: {}", reviewCode);

            // 권한 확인
            if (!canEditReview(reviewCode, memCode, guestCode)) {
                log.warn("리뷰 삭제 권한 없음 - reviewCode: {}", reviewCode);
                return false;
            }

            // 논리 삭제 (is_active = 0)
            int deletedRows = reviewDao.deleteReviewLogical(reviewCode);
            if (deletedRows > 0) {
                log.info("리뷰 삭제 완료 - reviewCode: {}", reviewCode);
                return true;
            } else {
                log.warn("리뷰 삭제 실패 - reviewCode: {}", reviewCode);
                return false;
            }

        } catch (Exception e) {
            log.error("리뷰 삭제 중 오류 발생 - reviewCode: {}", reviewCode, e);
            return false;
        }
    }
}