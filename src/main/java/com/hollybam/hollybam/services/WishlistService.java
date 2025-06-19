package com.hollybam.hollybam.services;

import com.hollybam.hollybam.dao.IF_WishlistDao;
import com.hollybam.hollybam.dao.IF_CertificationDao;
import com.hollybam.hollybam.dto.WishlistDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService implements IF_WishlistService {

    private final IF_WishlistDao wishlistDao;
    private final IF_CertificationDao certificationDao;

    @Override
    @Transactional
    public boolean toggleMemberWishlist(int memCode, int productCode) {
        try {
            // 현재 위시리스트에 있는지 확인
            boolean exists = wishlistDao.existsInWishlist(memCode, null, productCode) > 0;

            if (exists) {
                // 제거
                int result = wishlistDao.deleteWishlist(memCode, null, productCode);
                log.info("회원 위시리스트 제거 - memCode: {}, productCode: {}, result: {}",
                        memCode, productCode, result);
                return false;
            } else {
                // 추가
                int result = wishlistDao.insertWishlist(memCode, null, productCode);
                log.info("회원 위시리스트 추가 - memCode: {}, productCode: {}, result: {}",
                        memCode, productCode, result);
                return true;
            }
        } catch (Exception e) {
            log.error("회원 위시리스트 토글 실패 - memCode: {}, productCode: {}", memCode, productCode, e);
            throw new RuntimeException("위시리스트 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public boolean toggleGuestWishlist(int guestCode, int productCode) {
        try {
            // 현재 위시리스트에 있는지 확인
            boolean exists = wishlistDao.existsInWishlist(null, guestCode, productCode) > 0;

            if (exists) {
                // 제거
                int result = wishlistDao.deleteWishlist(null, guestCode, productCode);
                log.info("비회원 위시리스트 제거 - guestCode: {}, productCode: {}, result: {}",
                        guestCode, productCode, result);
                return false;
            } else {
                // 추가
                int result = wishlistDao.insertWishlist(null, guestCode, productCode);
                log.info("비회원 위시리스트 추가 - guestCode: {}, productCode: {}, result: {}",
                        guestCode, productCode, result);
                return true;
            }
        } catch (Exception e) {
            log.error("비회원 위시리스트 토글 실패 - guestCode: {}, productCode: {}", guestCode, productCode, e);
            throw new RuntimeException("위시리스트 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public boolean removeMemberWishlist(int memCode, int productCode) {
        try {
            int result = wishlistDao.deleteWishlist(memCode, null, productCode);
            log.info("회원 위시리스트 제거 - memCode: {}, productCode: {}, result: {}",
                    memCode, productCode, result);
            return result > 0;
        } catch (Exception e) {
            log.error("회원 위시리스트 제거 실패 - memCode: {}, productCode: {}", memCode, productCode, e);
            throw new RuntimeException("위시리스트 제거 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional
    public boolean removeGuestWishlist(int guestCode, int productCode) {
        try {
            int result = wishlistDao.deleteWishlist(null, guestCode, productCode);
            log.info("비회원 위시리스트 제거 - guestCode: {}, productCode: {}, result: {}",
                    guestCode, productCode, result);
            return result > 0;
        } catch (Exception e) {
            log.error("비회원 위시리스트 제거 실패 - guestCode: {}, productCode: {}", guestCode, productCode, e);
            throw new RuntimeException("위시리스트 제거 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<WishlistDto> getMemberWishlist(int memCode) {
        try {
            List<WishlistDto> wishlist = wishlistDao.selectMemberWishlist(memCode);
            log.debug("회원 위시리스트 조회 - memCode: {}, count: {}", memCode, wishlist.size());
            return wishlist;
        } catch (Exception e) {
            log.error("회원 위시리스트 조회 실패 - memCode: {}", memCode, e);
            throw new RuntimeException("위시리스트 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public List<WishlistDto> getGuestWishlist(int guestCode) {
        try {
            List<WishlistDto> wishlist = wishlistDao.selectGuestWishlist(guestCode);
            log.debug("비회원 위시리스트 조회 - guestCode: {}, count: {}", guestCode, wishlist.size());
            return wishlist;
        } catch (Exception e) {
            log.error("비회원 위시리스트 조회 실패 - guestCode: {}", guestCode, e);
            throw new RuntimeException("위시리스트 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public int getMemberWishlistCount(int memCode) {
        try {
            int count = wishlistDao.countMemberWishlist(memCode);
            log.debug("회원 위시리스트 개수 - memCode: {}, count: {}", memCode, count);
            return count;
        } catch (Exception e) {
            log.error("회원 위시리스트 개수 조회 실패 - memCode: {}", memCode, e);
            return 0;
        }
    }

    @Override
    public int getGuestWishlistCount(int guestCode) {
        try {
            int count = wishlistDao.countGuestWishlist(guestCode);
            log.debug("비회원 위시리스트 개수 - guestCode: {}, count: {}", guestCode, count);
            return count;
        } catch (Exception e) {
            log.error("비회원 위시리스트 개수 조회 실패 - guestCode: {}", guestCode, e);
            return 0;
        }
    }

    @Override
    public List<Integer> getWishlistProductCodes(Integer memCode, Integer guestCode, List<Integer> productCodes) {
        try {
            List<Integer> wishlistCodes = wishlistDao.selectWishlistProductCodes(memCode, guestCode, productCodes);
            log.debug("위시리스트 상품 코드 조회 - memCode: {}, guestCode: {}, requestCount: {}, resultCount: {}",
                    memCode, guestCode, productCodes.size(), wishlistCodes.size());
            return wishlistCodes;
        } catch (Exception e) {
            log.error("위시리스트 상품 코드 조회 실패 - memCode: {}, guestCode: {}", memCode, guestCode, e);
            throw new RuntimeException("위시리스트 상태 확인 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Integer getGuestCodeByUuid(String guestUuid) {
        try {
            // CertificationDao를 통해 guestUuid로 guestCode 조회
            Integer guestCode = certificationDao.selectGuestCodeByUuid(guestUuid);
            log.debug("UUID로 비회원 코드 조회 - uuid: {}, guestCode: {}", guestUuid, guestCode);
            return guestCode;
        } catch (Exception e) {
            log.error("UUID로 비회원 코드 조회 실패 - uuid: {}", guestUuid, e);
            return null;
        }
    }

    @Override
    public boolean isInWishlist(Integer memCode, Integer guestCode, int productCode) {
        try {
            boolean exists = wishlistDao.existsInWishlist(memCode, guestCode, productCode) > 0;
            log.debug("위시리스트 존재 확인 - memCode: {}, guestCode: {}, productCode: {}, exists: {}",
                    memCode, guestCode, productCode, exists);
            return exists;
        } catch (Exception e) {
            log.error("위시리스트 존재 확인 실패 - memCode: {}, guestCode: {}, productCode: {}",
                    memCode, guestCode, productCode, e);
            return false;
        }
    }
}