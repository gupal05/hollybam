package com.hollybam.hollybam.dao;

import com.hollybam.hollybam.dto.WishlistDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IF_WishlistDao {

    /**
     * 위시리스트에 상품 추가
     */
    int insertWishlist(@Param("memCode") Integer memCode,
                       @Param("guestCode") Integer guestCode,
                       @Param("productCode") int productCode);

    /**
     * 위시리스트에서 상품 제거
     */
    int deleteWishlist(@Param("memCode") Integer memCode,
                       @Param("guestCode") Integer guestCode,
                       @Param("productCode") int productCode);

    /**
     * 위시리스트 존재 여부 확인
     */
    int existsInWishlist(@Param("memCode") Integer memCode,
                         @Param("guestCode") Integer guestCode,
                         @Param("productCode") int productCode);

    /**
     * 회원의 위시리스트 목록 조회 (상품 정보 포함)
     */
    List<WishlistDto> selectMemberWishlist(@Param("memCode") int memCode);

    /**
     * 비회원의 위시리스트 목록 조회 (상품 정보 포함)
     */
    List<WishlistDto> selectGuestWishlist(@Param("guestCode") int guestCode);

    /**
     * 회원의 위시리스트 개수 조회
     */
    int countMemberWishlist(@Param("memCode") int memCode);

    /**
     * 비회원의 위시리스트 개수 조회
     */
    int countGuestWishlist(@Param("guestCode") int guestCode);

    /**
     * 여러 상품의 위시리스트 상태 조회 (한 번에 여러 상품 체크)
     */
    List<Integer> selectWishlistProductCodes(@Param("memCode") Integer memCode,
                                             @Param("guestCode") Integer guestCode,
                                             @Param("productCodes") List<Integer> productCodes);
}