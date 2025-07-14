package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.ReviewDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IF_ReviewService {
    boolean checkReviewEligibility(Integer orderItemCode, Integer memCode, Integer guestCode);
    void writeReview(ReviewDto reviewDto, List<MultipartFile> imageFiles) throws IOException;
    int isWroteReview(int orderItemCode);
}
