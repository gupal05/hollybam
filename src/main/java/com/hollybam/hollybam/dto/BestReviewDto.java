package com.hollybam.hollybam.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 베스트 리뷰 조회용 DTO
 * 리뷰 정보와 함께 상품, 작성자, 좋아요 수 등의 정보를 포함
 */
@Getter
@Setter
@ToString
public class BestReviewDto {
    // 리뷰 기본 정보
    private Integer reviewCode;
    private Integer memCode;
    private Integer guestCode;
    private Integer orderItemCode;
    private Integer rating;
    private String content;
    private String productId;
    private int likeCount;              // 좋아요 수
    private LocalDateTime createdAt;

    // 주문 아이템 정보
    private OrderItemDto orderItemDto;

    // 상품 정보
    private ProductDto productDto;

    // 상품 대표 이미지
    private ImageDto imageDto;

    // 회원 정보 (있는 경우)
    private MemberDto memberDto;

    // 비회원 정보 (있는 경우)
    private GuestDto guestDto;

    // 작성자 정보 (플랫 구조로도 제공)
    private String writerName;          // 작성자명
    private LocalDate writerBirth;      // 작성자 생년월일
    private String writerType;          // 작성자 타입 (member/guest)
    private String writerAge;

    // ⭐ 새로 추가: 사용자별 좋아요 상태 (기존 코드는 변경 없음)
    private boolean isLiked;            // 현재 사용자가 이 리뷰에 좋아요를 눌렀는지 여부
}