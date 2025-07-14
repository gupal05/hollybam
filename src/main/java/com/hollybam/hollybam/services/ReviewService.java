package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_ReviewDao;
import com.hollybam.hollybam.dto.ReviewDto;
import com.hollybam.hollybam.dto.ReviewImageDto;
import com.hollybam.hollybam.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IF_ReviewService{
    private final IF_ReviewDao reviewDao;
    private final S3Uploader s3Uploader;

    @Override
    public boolean checkReviewEligibility(Integer orderItemCode, Integer memCode, Integer guestCode) {
        // 1. 주문 상태 확인 (배송완료인지)
        String orderStatus = reviewDao.getOrderStatusByOrderItemCode(orderItemCode);
        if (!"DELIVERED".equals(orderStatus)) return false;

        // 2. 이미 리뷰가 작성되었는지 확인
        int existingCount = reviewDao.countExistingReview(orderItemCode, memCode, guestCode);
        return existingCount == 0;
    }

    @Override
    public void writeReview(ReviewDto reviewDto, List<MultipartFile> imageFiles) throws IOException {
        // 1. 리뷰 저장
        reviewDao.insertReview(reviewDto);

        // 2. 이미지가 있다면 S3 저장 및 DB insert
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imageUrl = s3Uploader.upload(file, "review");

                    ReviewImageDto imageDto = new ReviewImageDto();
                    imageDto.setReviewCode(reviewDto.getReviewCode()); // insert 후 key 반환되어 있어야 함
                    imageDto.setImageUrl(imageUrl);

                    reviewDao.insertReviewImage(imageDto);
                }
            }
        }
    }

    public int isWroteReview(int orderItemCode){
        return reviewDao.isWroteReview(orderItemCode);
    }
}
