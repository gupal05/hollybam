// ===== 기존 함수들 100% 유지하되 반응형 로직 수정 =====

function updateHeaderState() {
    const header = $('.header');
    const dropdown = $('.dropdown');
    const backdrop = $('.dropdown-backdrop');

    // 900px 이하에서는 모바일 모드
    if (window.innerWidth <= 900) {
        header.css('height', 'auto');
        header.removeClass('header-scrolled');
    } else if ($(window).scrollTop() > 0) {
        header.css('height', '58.5px');
        header.addClass('header-scrolled');
        backdrop.css('top', '58.5px');
        backdrop.css('height', 'calc(100vh - 58.5px)');
    } else {
        header.css('height', '105.8px');
        header.removeClass('header-scrolled');
        backdrop.css('top', '105.8px');
        backdrop.css('height', 'calc(100vh - 105.8px)');
    }
}

$(window).on('scroll resize', updateHeaderState);
$(document).ready(updateHeaderState);

function goLogin() {
    const form = document.createElement('form');
    form.method = 'get';
    form.action = '/auth/login';
    document.body.appendChild(form);
    form.submit();
}

// 사용자 드롭다운 기능 (기존 100% 유지하되 모바일 기준 수정)
$(document).ready(function () {
    const $userNavItem = $('.user-nav-item');
    const $userIcon = $userNavItem.find('i');
    const $body = $('body');

    function isMobile() {
        return window.innerWidth <= 900; // 900px 이하를 모바일로 간주
    }

    function createOverlay() {
        if ($userNavItem.find('.user-dropdown-overlay').length === 0) {
            const $overlay = $('<div class="user-dropdown-overlay"></div>');
            $userNavItem.append($overlay);
        }
    }

    function toggleDropdown() {
        if (isMobile()) {
            $userNavItem.toggleClass('active');
            $body.css('overflow', $userNavItem.hasClass('active') ? 'hidden' : '');
        }
    }

    function closeDropdown() {
        $userNavItem.removeClass('active');
        $body.css('overflow', '');
    }

    if (isMobile()) {
        createOverlay();

        $userIcon.on('click', function (e) {
            e.stopPropagation();
            toggleDropdown();
        });

        $userNavItem.on('click', '.user-dropdown-overlay', function () {
            closeDropdown();
        });

        $userNavItem.find('.user-dropdown').on('click', function (e) {
            e.stopPropagation();
        });
    }

    let resizeTimer;
    $(window).on('resize', function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(function () {
            if (!isMobile()) {
                closeDropdown();
                $userNavItem.find('.user-dropdown-overlay').remove();
            } else {
                createOverlay();

                $userIcon.off('click').on('click', function (e) {
                    e.stopPropagation();
                    toggleDropdown();
                });
            }
        }, 250);
    });

    $(document).on('keydown', function (e) {
        if (e.key === 'Escape' && $userNavItem.hasClass('active')) {
            closeDropdown();
        }
    });

    $userNavItem.find('.user-dropdown-link').on('click', function () {
        if (isMobile()) {
            closeDropdown();
        }
    });

    // ✅ 모바일 카테고리 드롭다운 토글 기능 (기존 유지하되 모바일 기준 수정)
    function bindMobileCategoryDropdown() {
        if (isMobile()) {
            $('.nav-item').each(function () {
                const $item = $(this);
                $item.off('click').on('click', function (e) {
                    e.stopPropagation();
                    // 다른 메뉴 닫기
                    $('.nav-item').not($item).removeClass('active');
                    // 현재 메뉴 toggle
                    $item.toggleClass('active');
                });
            });

            // 외부 클릭 시 모두 닫기
            $(document).on('click.mobileDropdown', function () {
                $('.nav-item').removeClass('active');
            });
        } else {
            // 데스크톱에서는 이벤트 제거
            $('.nav-item').off('click');
            $(document).off('click.mobileDropdown');
            $('.nav-item').removeClass('active');
        }
    }

    // 처음 로딩 시
    bindMobileCategoryDropdown();

    // 리사이즈 시 다시 바인딩
    $(window).on('resize', function () {
        bindMobileCategoryDropdown();
    });
});

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        location.href = "/auth/logout";
    }
}

// ===== 모바일 메뉴 기능 (수정) =====

$(document).ready(function() {
    const $mobileMenuBtn = $('#mobileMenuBtn');
    const $mobileMenu = $('#mobileMenu');
    const $mobileOverlay = $('#mobileOverlay');
    const $body = $('body');

    // 모바일 여부 확인 함수
    function isMobileMenuActive() {
        return window.innerWidth <= 900; // 900px 이하에서 모바일 메뉴 활성화
    }

    // 모바일 메뉴 토글
    function toggleMobileMenu() {
        if (!isMobileMenuActive()) return; // 데스크톱에서는 작동하지 않음

        const isActive = $mobileMenu.hasClass('active');

        if (isActive) {
            closeMobileMenu();
        } else {
            openMobileMenu();
        }
    }

    // 모바일 메뉴 열기
    function openMobileMenu() {
        if (!isMobileMenuActive()) return;

        $mobileMenuBtn.addClass('active');
        $mobileMenu.addClass('active');
        $mobileOverlay.addClass('active');
        $body.css('overflow', 'hidden');
    }

    // 모바일 메뉴 닫기
    function closeMobileMenu() {
        $mobileMenuBtn.removeClass('active');
        $mobileMenu.removeClass('active');
        $mobileOverlay.removeClass('active');
        $body.css('overflow', '');

        // 카테고리 메뉴도 닫기
        $('#mobileCategoryItems').removeClass('active');
        $('.mobile-arrow').removeClass('active');
    }

    // 이벤트 리스너
    $mobileMenuBtn.on('click', function(e) {
        e.stopPropagation();
        toggleMobileMenu();
    });

    $mobileOverlay.on('click', closeMobileMenu);

    // ESC 키로 메뉴 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $mobileMenu.hasClass('active')) {
            closeMobileMenu();
        }
    });

    // 창 크기 변경 시 모바일 메뉴 처리
    $(window).on('resize', function() {
        if (!isMobileMenuActive() && $mobileMenu.hasClass('active')) {
            // 데스크톱으로 변경 시 모바일 메뉴 닫기
            closeMobileMenu();
        }
    });

    // 메뉴 링크 클릭 시 메뉴 닫기
    $mobileMenu.find('a').on('click', function() {
        closeMobileMenu();
    });

    // 모바일 네비게이션 아이템 클릭 시 메뉴 닫기 (카테고리 제외)
    $mobileMenu.find('.mobile-nav-item').not('[onclick*="toggleMobileCategory"]').on('click', function() {
        closeMobileMenu();
    });
});

// 모바일 카테고리 토글 함수 (수정)
function toggleMobileCategory() {
    const $categoryItems = $('#mobileCategoryItems');
    const $arrow = $('.mobile-arrow');

    $categoryItems.toggleClass('active');
    $arrow.toggleClass('active');

    // 카테고리가 열릴 때 스크롤 위치 조정
    if ($categoryItems.hasClass('active')) {
        setTimeout(() => {
            const categoryElement = document.getElementById('mobileCategoryItems');
            if (categoryElement) {
                categoryElement.scrollIntoView({
                    behavior: 'smooth',
                    block: 'nearest'
                });
            }
        }, 100);
    }
}

// ===== 추가 최적화 함수들 =====

// 헤더 고정 스크롤 처리 개선
$(window).on('scroll', function() {
    if (window.innerWidth > 900) { // 데스크톱에서만 적용
        const scrollTop = $(this).scrollTop();
        const $header = $('.header');

        if (scrollTop > 100) {
            $header.addClass('header-scrolled');
        } else {
            $header.removeClass('header-scrolled');
        }
    }
});

// 터치 디바이스 최적화
if ('ontouchstart' in window) {
    // 터치 디바이스에서 호버 효과 개선
    $('.nav-item, .mobile-nav-item').on('touchstart', function() {
        $(this).addClass('touch-active');
    });

    $('.nav-item, .mobile-nav-item').on('touchend', function() {
        const $this = $(this);
        setTimeout(() => {
            $this.removeClass('touch-active');
        }, 150);
    });
}

// 성능 최적화: 디바운싱된 리사이즈 핸들러
let resizeTimeout;
$(window).on('resize', function() {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(function() {
        updateHeaderState();

        // 모바일 메뉴가 열려있는 상태에서 데스크톱으로 변경되면 닫기
        if (window.innerWidth > 900 && $('#mobileMenu').hasClass('active')) {
            $('#mobileMenu').removeClass('active');
            $('#mobileOverlay').removeClass('active');
            $('#mobileMenuBtn').removeClass('active');
            $('body').css('overflow', '');
        }
    }, 100);
});

// 카테고리 드롭다운 개선 (데스크톱)
$(document).ready(function() {
    // 데스크톱에서 카테고리 호버 시 전체 카테고리 표시
    $('.category-nav-item').on('mouseenter', function() {
        if (window.innerWidth > 900) {
            const $dropdown = $(this).find('.dropdown');
            const $backdrop = $(this).find('.dropdown-backdrop');

            $backdrop.css('pointer-events', 'all');
            $dropdown.css({
                'opacity': '1',
                'visibility': 'visible'
            });
        }
    });

    $('.category-nav-item').on('mouseleave', function() {
        if (window.innerWidth > 900) {
            const $dropdown = $(this).find('.dropdown');
            const $backdrop = $(this).find('.dropdown-backdrop');

            setTimeout(() => {
                if (!$dropdown.is(':hover') && !$(this).is(':hover')) {
                    $backdrop.css('pointer-events', 'none');
                    $dropdown.css({
                        'opacity': '0',
                        'visibility': 'hidden'
                    });
                }
            }, 100);
        }
    });

    // 드롭다운 자체에서 마우스가 나갔을 때도 처리
    $('.dropdown').on('mouseleave', function() {
        if (window.innerWidth > 900) {
            const $this = $(this);
            const $backdrop = $('.dropdown-backdrop');

            setTimeout(() => {
                if (!$('.category-nav-item').is(':hover')) {
                    $backdrop.css('pointer-events', 'none');
                    $this.css({
                        'opacity': '0',
                        'visibility': 'hidden'
                    });
                }
            }, 100);
        }
    });
});

// 모바일 메뉴 접근성 개선
$(document).ready(function() {
    const $mobileMenu = $('#mobileMenu');
    const focusableElements = $mobileMenu.find('a, button, [tabindex]:not([tabindex="-1"])');

    if (focusableElements.length > 0) {
        const firstElement = focusableElements.first();
        const lastElement = focusableElements.last();

        $mobileMenu.on('keydown', function(e) {
            if (e.key === 'Tab') {
                if (e.shiftKey) {
                    if (document.activeElement === firstElement[0]) {
                        e.preventDefault();
                        lastElement.focus();
                    }
                } else {
                    if (document.activeElement === lastElement[0]) {
                        e.preventDefault();
                        firstElement.focus();
                    }
                }
            }
        });

        // 모바일 메뉴가 열릴 때 첫 번째 요소에 포커스
        $('#mobileMenuBtn').on('click', function() {
            if ($mobileMenu.hasClass('active')) {
                setTimeout(() => {
                    firstElement.focus();
                }, 300);
            }
        });
    }
});

// 부드러운 스크롤 효과
function smoothScrollToTop() {
    $('html, body').animate({
        scrollTop: 0
    }, 500);
}

// 로고 클릭 시 부드러운 스크롤
$(document).ready(function() {
    $('.header-nav:first-child').on('click', function(e) {
        if (window.location.pathname === '/main' || window.location.pathname === '/') {
            e.preventDefault();
            smoothScrollToTop();
        }
    });
});

// 모바일 스와이프 제스처 지원 (선택사항)
if ('ontouchstart' in window) {
    let startX = 0;
    let startY = 0;

    $('#mobileMenu').on('touchstart', function(e) {
        startX = e.touches[0].clientX;
        startY = e.touches[0].clientY;
    });

    $('#mobileMenu').on('touchmove', function(e) {
        const currentX = e.touches[0].clientX;
        const currentY = e.touches[0].clientY;
        const diffX = startX - currentX;
        const diffY = Math.abs(startY - currentY);

        // 오른쪽으로 스와이프하면 메뉴 닫기
        if (diffX < -50 && diffY < 100) {
            $('#mobileMenu').removeClass('active');
            $('#mobileOverlay').removeClass('active');
            $('#mobileMenuBtn').removeClass('active');
            $('body').css('overflow', '');
        }
    });
}