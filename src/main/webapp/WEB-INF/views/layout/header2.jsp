<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="header">
    <!-- 로고 영역 -->
    <div class="header-nav" onclick="movePage()" style="cursor: pointer">
        <img src="${pageContext.request.contextPath}/images/hollybam_logo.webp"
             width="85px" height="27.42px" alt="Hollybam Logo">
    </div>

    <!-- 네비게이션 영역 -->
    <div class="header-nav nav-group">
        <div class="nav-item">
            <div>제품</div>
            <div class="dropdown-backdrop"></div>
            <div class="dropdown">
                <div class="dropdown-content">
                    <c:forEach var="category" items="${categories}">
                        <div class="dropdown-column">
                            <a href="/product/list?categoryCode=${category.categoryCode}"
                               class="dropdown-column-title-link">
                                <div class="dropdown-column-title">${category.categoryName}</div>
                            </a>
                            <c:forEach var="detail" items="${category.categoryDetailDto}">
                                <div class="dropdown-item">
                                    <a href="/product/list?categoryCode=${category.categoryCode}${detail.cateDetailCode}"
                                       class="category-link">
                                            ${detail.cateDetailName}
                                    </a>
                                </div>
                            </c:forEach>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
        <div onclick="goToBrand()">브랜드관</div>
        <div onclick="goToSpecial()">코스튬 특가</div>
    </div>

    <!-- 모바일 햄버거 메뉴 토글 (모바일에서만 표시) -->
    <div class="mobile-menu-toggle" onclick="toggleMobileMenu()">
        <span></span>
        <span></span>
        <span></span>
    </div>

    <!-- 아이콘 영역 -->
    <div class="header-nav nav-group-icon">
        <i class="fa-regular fa-circle-user" onclick="goLogin();" title="로그인"></i>
        <i class="fa-solid fa-magnifying-glass" onclick="toggleSearch();" title="검색"></i>
        <i class="fa-solid fa-bag-shopping" onclick="goToCart();" title="장바구니"></i>
    </div>
</div>

<!-- 검색 오버레이 (선택사항) -->
<div class="search-overlay" id="searchOverlay">
    <div class="search-container">
        <div class="search-box">
            <input type="text" placeholder="상품을 검색해보세요..." class="search-input" id="searchInput">
            <button class="search-btn" onclick="performSearch()">
                <i class="fa-solid fa-magnifying-glass"></i>
            </button>
        </div>
        <div class="search-close" onclick="toggleSearch()">
            <i class="fa-solid fa-xmark"></i>
        </div>
    </div>
</div>

<script>
    // 헤더 스크롤 상태 관리
    function updateHeaderState() {
        const header = document.querySelector('.header');
        const backdrop = document.querySelector('.dropdown-backdrop');

        if (window.innerWidth <= 768) {
            header.style.height = '60px';
            header.classList.remove('header-scrolled');
        } else if (window.scrollY > 50) {
            header.style.height = '64px';
            header.classList.add('header-scrolled');
            if (backdrop) {
                backdrop.style.top = '64px';
                backdrop.style.height = 'calc(100vh - 64px)';
            }
        } else {
            header.style.height = '80px';
            header.classList.remove('header-scrolled');
            if (backdrop) {
                backdrop.style.top = '80px';
                backdrop.style.height = 'calc(100vh - 80px)';
            }
        }
    }

    // 모바일 메뉴 토글
    function toggleMobileMenu() {
        const navGroup = document.querySelector('.nav-group');
        const toggle = document.querySelector('.mobile-menu-toggle');

        navGroup.classList.toggle('active');
        toggle.classList.toggle('active');
    }

    // 검색 오버레이 토글
    function toggleSearch() {
        const overlay = document.getElementById('searchOverlay');
        const input = document.getElementById('searchInput');

        if (overlay.classList.contains('active')) {
            overlay.classList.remove('active');
            document.body.style.overflow = '';
        } else {
            overlay.classList.add('active');
            document.body.style.overflow = 'hidden';
            setTimeout(() => input.focus(), 300);
        }
    }

    // 검색 실행
    function performSearch() {
        const query = document.getElementById('searchInput').value.trim();
        if (query) {
            window.location.href = '/product/search?q=' + encodeURIComponent(query);
        }
    }

    // 엔터키로 검색
    document.addEventListener('DOMContentLoaded', function() {
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    performSearch();
                }
            });
        }
    });

    // 네비게이션 함수들
    function goLogin() {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/auth/login';
        document.body.appendChild(form);
        form.submit();
    }

    function movePage() {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/main';
        document.body.appendChild(form);
        form.submit();
    }

    function goToBrand() {
        window.location.href = '/brand';
    }

    function goToSpecial() {
        window.location.href = '/special';
    }

    function goToCart() {
        window.location.href = '/cart';
    }

    // 이벤트 리스너 등록
    window.addEventListener('scroll', updateHeaderState);
    window.addEventListener('resize', updateHeaderState);
    document.addEventListener('DOMContentLoaded', updateHeaderState);

    // 모바일에서 메뉴 외부 클릭시 닫기
    document.addEventListener('click', function(e) {
        const navGroup = document.querySelector('.nav-group');
        const toggle = document.querySelector('.mobile-menu-toggle');

        if (window.innerWidth <= 768 &&
            navGroup.classList.contains('active') &&
            !navGroup.contains(e.target) &&
            !toggle.contains(e.target)) {
            toggleMobileMenu();
        }
    });
</script>

<style>
    /* 검색 오버레이 스타일 */
    .search-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.8);
        backdrop-filter: blur(10px);
        z-index: 10000;
        display: flex;
        align-items: center;
        justify-content: center;
        opacity: 0;
        visibility: hidden;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .search-overlay.active {
        opacity: 1;
        visibility: visible;
    }

    .search-container {
        position: relative;
        width: 90%;
        max-width: 600px;
    }

    .search-box {
        display: flex;
        align-items: center;
        background: white;
        border-radius: 50px;
        padding: 8px;
        box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    }

    .search-input {
        flex: 1;
        border: none;
        outline: none;
        padding: 16px 24px;
        font-size: 18px;
        background: transparent;
        color: #333;
    }

    .search-input::placeholder {
        color: #999;
    }

    .search-btn {
        background: linear-gradient(135deg, #ec4899, #f472b6);
        border: none;
        border-radius: 50%;
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: transform 0.2s ease;
    }

    .search-btn:hover {
        transform: scale(1.05);
    }

    .search-btn i {
        color: white;
        font-size: 18px;
    }

    .search-close {
        position: absolute;
        top: -60px;
        right: 0;
        width: 40px;
        height: 40px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s ease;
    }

    .search-close:hover {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(1.1);
    }

    .search-close i {
        color: white;
        font-size: 20px;
    }

    /* 모바일에서 햄버거 메뉴 숨기기 (데스크톱) */
    .mobile-menu-toggle {
        display: none;
    }

    @media (max-width: 768px) {
        .mobile-menu-toggle {
            display: flex;
        }

        .search-input {
            font-size: 16px;
            padding: 12px 20px;
        }

        .search-btn {
            width: 40px;
            height: 40px;
        }

        .search-btn i {
            font-size: 16px;
        }
    }
</style>