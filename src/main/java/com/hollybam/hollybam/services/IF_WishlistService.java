package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dto.WishlistDto;

import java.util.List;

public interface IF_WishlistService {

    /**
     * 회원 위시리스트 토글 (추가/제거)
     * @param memCode 회원 코드
     * @param productCode 상품 코드
     * @return true: 추가됨, false: 제거됨
     */
    boolean toggleMemberWishlist(int memCode, int productCode);

    /**
     * 비회원 위시리스트 토글 (추가/제거)
     * @param guestCode 비회원 코드
     * @param productCode 상품 코드
     * @return true: 추가됨, false: 제거됨
     */
    boolean toggleGuestWishlist(int guestCode, int productCode);

    /**
     * 회원 위시리스트에서 상품 제거
     */
    boolean removeMemberWishlist(int memCode, int productCode);

    /**
     * 비회원 위시리스트에서 상품 제거
     */
    boolean removeGuestWishlist(int guestCode, int productCode);

    /**
     * 회원의 위시리스트 목록 조회
     */
    List<WishlistDto> getMemberWishlist(int memCode);

    /**
     * 비회원의 위시리스트 목록 조회
     */
    List<WishlistDto> getGuestWishlist(int guestCode);

    /**
     * 회원의 위시리스트 개수 조회
     */
    int getMemberWishlistCount(int memCode);

    /**
     * 비회원의 위시리스트 개수 조회
     */
    int getGuestWishlistCount(int guestCode);

    /**
     * 여러 상품의 위시리스트 상태 조회
     */
    List<Integer> getWishlistProductCodes(Integer memCode, Integer guestCode, List<Integer> productCodes);

    /**
     * 위시리스트 존재 여부 확인
     */
    boolean isInWishlist(Integer memCode, Integer guestCode, int productCode);
}