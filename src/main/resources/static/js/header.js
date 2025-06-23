function updateHeaderState() {
    const header = $('.header');
    const dropdown = $('.dropdown');
    const backdrop = $('.dropdown-backdrop');

    if (window.innerWidth <= 768) {
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
    form.method = 'POST';
    form.action = '/auth/login';
    document.body.appendChild(form);
    form.submit();
}

// 사용자 드롭다운 기능
$(document).ready(function () {
    const $userNavItem = $('.user-nav-item');
    const $userIcon = $userNavItem.find('i');
    const $body = $('body');

    function isMobile() {
        return window.innerWidth <= 768;
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

    // ✅ 모바일 카테고리 드롭다운 토글 기능 추가
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
