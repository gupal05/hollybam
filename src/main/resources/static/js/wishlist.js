/**
 * 위시리스트 관리 JavaScript
 */

// 위시리스트 상태를 관리하는 객체
const WishlistManager = {

    /**
     * 페이지 로드 시 초기화
     */
    init() {
        console.log('WishlistManager 초기화 시작');
        this.bindEvents();
        this.loadWishlistStatus();
    },

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        // 위시리스트 버튼 클릭 이벤트
        $(document).on('click', '.wishlist-btn', (e) => {
            e.preventDefault();
            e.stopPropagation();

            const $btn = $(e.currentTarget);
            const productCode = parseInt($btn.data('product-code'));

            console.log('위시리스트 버튼 클릭 - productCode:', productCode);

            if (!productCode) {
                console.error('상품 코드를 찾을 수 없습니다.');
                return;
            }

            this.toggleWishlist(productCode, $btn);
        });
    },

    /**
     * 위시리스트 토글 (추가/제거)
     */
    toggleWishlist(productCode, $btn) {
        // 버튼 비활성화 (중복 클릭 방지)
        $btn.prop('disabled', true);

        $.ajax({
            url: '/wishlist/toggle',
            type: 'POST',
            data: { productCode: productCode },
            success: (response) => {
                console.log('위시리스트 토글 성공:', response);

                if (response.success) {
                    // UI 업데이트
                    this.updateHeartButton($btn, response.isInWishlist);

                    // 성공 메시지 표시
                    this.showMessage(response.message, 'success');

                    // 위시리스트 페이지라면 카운트 업데이트
                    this.updateWishlistCount();

                } else {
                    // 인증이 필요한 경우
                    if (response.requireAuth) {
                        if (confirm('위시리스트 기능을 사용하려면 로그인 또는 성인인증이 필요합니다.\n로그인 페이지로 이동하시겠습니까?')) {
                            window.location.href = '/intro';
                        }
                    } else {
                        this.showMessage(response.message, 'error');
                    }
                }
            },
            error: (xhr) => {
                console.error('위시리스트 토글 실패:', xhr);
                this.showMessage('처리 중 오류가 발생했습니다.', 'error');
            },
            complete: () => {
                // 버튼 활성화
                $btn.prop('disabled', false);
            }
        });
    },

    /**
     * 하트 버튼 UI 업데이트
     */
    updateHeartButton($btn, isInWishlist) {
        const $heart = $btn.find('.heart-icon');

        if (isInWishlist) {
            // 빨간 하트로 변경 (채워진 하트)
            $heart.removeClass('far').addClass('fas').css('color', '#e74c3c');
            $btn.addClass('active').attr('title', '위시리스트에서 제거');
            console.log('하트 버튼을 빨간색으로 변경');
        } else {
            // 빈 하트로 변경
            $heart.removeClass('fas').addClass('far').css('color', '#999');
            $btn.removeClass('active').attr('title', '위시리스트에 추가');
            console.log('하트 버튼을 빈 하트로 변경');
        }
    },

    /**
     * 페이지의 모든 위시리스트 상태 로드 (중요!)
     */
    loadWishlistStatus() {
        console.log('위시리스트 상태 로드 시작');

        const productCodes = [];
        $('.wishlist-btn').each(function() {
            const productCode = parseInt($(this).data('product-code'));
            if (productCode && !productCodes.includes(productCode)) {
                productCodes.push(productCode);
            }
        });

        console.log('찾은 상품 코드들:', productCodes);

        if (productCodes.length === 0) {
            console.log('위시리스트 버튼이 없어서 상태 로드 생략');
            return;
        }

        $.ajax({
            url: '/wishlist/status',
            type: 'POST',
            data: { productCodes: productCodes },
            success: (response) => {
                console.log('위시리스트 상태 로드 성공:', response);

                if (response.success) {
                    const wishlistProductCodes = response.wishlistProductCodes || [];
                    console.log('위시리스트에 있는 상품들:', wishlistProductCodes);

                    $('.wishlist-btn').each((index, btn) => {
                        const $btn = $(btn);
                        const productCode = parseInt($btn.data('product-code'));
                        const isInWishlist = wishlistProductCodes.includes(productCode);

                        console.log(`상품 ${productCode}: 위시리스트 ${isInWishlist ? '있음' : '없음'}`);

                        this.updateHeartButton($btn, isInWishlist);
                    });
                }
            },
            error: (xhr) => {
                console.error('위시리스트 상태 로드 실패:', xhr);
            }
        });
    },

    /**
     * 위시리스트 개수 업데이트
     */
    updateWishlistCount() {
        const $countElement = $('.wishlist-count');
        if ($countElement.length > 0) {
            $.ajax({
                url: '/wishlist/count',
                type: 'GET',
                success: (response) => {
                    if (response.success) {
                        $countElement.text(response.count);
                    }
                }
            });
        }
    },

    /**
     * 메시지 표시 (토스트나 알림)
     */
    showMessage(message, type = 'info') {
        // 간단한 토스트 메시지 구현
        const $toast = $(`
            <div class="toast-message toast-${type}">
                <span>${message}</span>
            </div>
        `);

        $('body').append($toast);

        // 애니메이션과 자동 제거
        setTimeout(() => {
            $toast.addClass('show');
        }, 100);

        setTimeout(() => {
            $toast.removeClass('show');
            setTimeout(() => $toast.remove(), 300);
        }, 3000);
    }
};

// CSS 스타일 추가
const wishlistStyles = `
<style>
/* 위시리스트 버튼 스타일 */
.wishlist-btn {
    background: none;
    border: none;
    padding: 8px;
    cursor: pointer;
    border-radius: 50%;
    transition: all 0.3s ease;
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
}

.wishlist-btn:hover {
    background-color: rgba(0, 0, 0, 0.1);
    transform: scale(1.1);
}

.wishlist-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.wishlist-btn .heart-icon {
    font-size: 18px;
    transition: all 0.3s ease;
}

.wishlist-btn.active .heart-icon {
    color: #e74c3c !important;
    animation: heartBeat 0.6s ease-in-out;
}

/* 하트 애니메이션 */
@keyframes heartBeat {
    0% { transform: scale(1); }
    25% { transform: scale(1.2); }
    50% { transform: scale(1.1); }
    75% { transform: scale(1.15); }
    100% { transform: scale(1); }
}

/* 토스트 메시지 스타일 */
.toast-message {
    position: fixed;
    top: 20px;
    right: 20px;
    background: #333;
    color: white;
    padding: 12px 20px;
    border-radius: 6px;
    z-index: 9999;
    opacity: 0;
    transform: translateX(100%);
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.toast-message.show {
    opacity: 1;
    transform: translateX(0);
}

.toast-message.toast-success {
    background: #27ae60;
}

.toast-message.toast-error {
    background: #e74c3c;
}

.toast-message.toast-info {
    background: #3498db;
}
</style>
`;

// 페이지 로드 시 초기화
$(document).ready(() => {
    console.log('페이지 로드 완료 - WishlistManager 초기화');

    // 스타일 추가
    $('head').append(wishlistStyles);

    // 위시리스트 매니저 초기화
    WishlistManager.init();
});