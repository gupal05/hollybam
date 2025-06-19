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
$(document).ready(function() {
    const $userNavItem = $('.user-nav-item');
    const $userIcon = $userNavItem.find('i');
    const $body = $('body');

    // 모바일 체크
    function isMobile() {
        return window.innerWidth <= 768;
    }

    // 오버레이 생성
    function createOverlay() {
        if ($userNavItem.find('.user-dropdown-overlay').length === 0) {
            const $overlay = $('<div class="user-dropdown-overlay"></div>');
            $userNavItem.append($overlay);
        }
    }

    // 드롭다운 토글
    function toggleDropdown() {
        if (isMobile()) {
            $userNavItem.toggleClass('active');
            $body.css('overflow', $userNavItem.hasClass('active') ? 'hidden' : '');
        }
    }

    // 드롭다운 닫기
    function closeDropdown() {
        $userNavItem.removeClass('active');
        $body.css('overflow', '');
    }

    // 모바일 환경 설정
    if (isMobile()) {
        createOverlay();

        // 아이콘 클릭 이벤트
        $userIcon.on('click', function(e) {
            e.stopPropagation();
            toggleDropdown();
        });

        // 오버레이 클릭 시 닫기
        $userNavItem.on('click', '.user-dropdown-overlay', function() {
            closeDropdown();
        });

        // 드롭다운 내부 클릭 시 이벤트 전파 중지
        $userNavItem.find('.user-dropdown').on('click', function(e) {
            e.stopPropagation();
        });
    }

    // 윈도우 리사이즈 시 상태 초기화
    let resizeTimer;
    $(window).on('resize', function() {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(function() {
            if (!isMobile()) {
                closeDropdown();
                // 데스크톱으로 전환 시 오버레이 제거
                $userNavItem.find('.user-dropdown-overlay').remove();
            } else {
                // 모바일로 전환 시 오버레이 추가
                createOverlay();

                // 모바일 이벤트 다시 바인딩
                $userIcon.off('click').on('click', function(e) {
                    e.stopPropagation();
                    toggleDropdown();
                });
            }
        }, 250);
    });

    // ESC 키로 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $userNavItem.hasClass('active')) {
            closeDropdown();
        }
    });

    // 모바일에서 링크 클릭 시 드롭다운 닫기
    $userNavItem.find('.user-dropdown-link').on('click', function() {
        if (isMobile()) {
            closeDropdown();
        }
    });
});

function logout(){
    if(confirm('로그아웃 하시겠습니까?')){
        location.href = "/auth/logout";
    }
}