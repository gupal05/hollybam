package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 상세 조회용 DTO
 * 리뷰 정보와 함께 상품, 작성자, 이미지, 좋아요 정보를 포함
 */
@Getter
@Setter
@ToString
public class ReviewDetailDto {
    // 리뷰 기본 정보
    private Integer reviewCode;
    private Integer memCode;
    private Integer guestCode;
    private Integer orderItemCode;
    private Integer rating;
    private String content;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;              // 좋아요 수

    // 상품 정보
    private ProductDto productDto;

    // 상품 옵션 정보 (있는 경우)
    private ProductOptionDto productOptionDto;

    // 상품 대표 이미지
    private ImageDto productImageDto;

    // 회원 정보 (있는 경우)
    private MemberDto memberDto;

    // 비회원 정보 (있는 경우)
    private GuestDto guestDto;

    // 리뷰 이미지 목록
    private List<ReviewImageDto> reviewImages;

    // 작성자 정보 (가공된 정보)
    private String writerName;          // 마스킹된 작성자명
    private String writerAge;           // 연령대 (20대, 30대 등)
    private String writerGender;        // 성별
    private String writerType;          // 작성자 타입 (member/guest)

    // 현재 사용자 관련 정보
    private boolean isLiked;            // 현재 사용자가 좋아요를 눌렀는지
    private boolean canEdit;            // 수정 가능 여부 (본인 리뷰인지)
    private boolean canDelete;          // 삭제 가능 여부 (본인 리뷰인지)

    // 주문 정보
    private OrderItemDto orderItemDto;
}