function toggleSubmenu(element) {
    const submenu = element.nextElementSibling;
    const isOpen = submenu.classList.contains('open');

    // 모든 서브메뉴 닫기
    document.querySelectorAll('.submenu').forEach(menu => {
        menu.classList.remove('open');
    });
    document.querySelectorAll('.nav-link.has-submenu').forEach(link => {
        link.classList.remove('open');
    });

    // 클릭한 서브메뉴만 토글
    if (!isOpen) {
        submenu.classList.add('open');
        element.classList.add('open');
    }
}

function logout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        window.location.href = '/admin/logout';
    }
}

// 반응형 사이드바 토글
function toggleSidebar() {
    const sidebar = document.querySelector('.sidebar');
    sidebar.classList.toggle('open');
}

// 모바일에서 사이드바 외부 클릭 시 닫기
document.addEventListener('click', function(event) {
    const sidebar = document.querySelector('.sidebar');
    const isClickInsideSidebar = sidebar.contains(event.target);

    if (!isClickInsideSidebar && window.innerWidth <= 768) {
        sidebar.classList.remove('open');
    }
});

// 페이지 로드 시 현재 경로에 맞는 서브메뉴 자동 열기
document.addEventListener('DOMContentLoaded', function() {
    const activeSubmenuLink = document.querySelector('.submenu-link.active');
    if (activeSubmenuLink) {
        const parentSubmenu = activeSubmenuLink.closest('.submenu');
        const parentNavLink = parentSubmenu.previousElementSibling;

        parentSubmenu.classList.add('open');
        parentNavLink.classList.add('open');
    }
});