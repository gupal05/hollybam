<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="header">
    <div class="header-nav" onclick="window.location.href='/main'" style="cursor: pointer">
        <img src="${pageContext.request.contextPath}/images/hollybam_logo.webp" width="85px;" height="27.42px;">
    </div>
    <div class="header-nav nav-group">
        <div class="nav-item">
            <div>제품</div>
            <div class="dropdown-backdrop"></div>
            <div class="dropdown">
                <div class="dropdown-content">
                    <!-- 첫 번째 컬럼 -->
                    <c:forEach var="category" items="${categories}">
                        <div class="dropdown-column">
                            <a href="/product/list?categoryCode=${category.categoryCode}" class="dropdown-column-title-link">
                                <div class="dropdown-column-title">${category.categoryName}</div>
                            </a>
                            <c:forEach var="detail" items="${category.categoryDetailDto}">
                                <div class="dropdown-item">
                                    <a href="/product/list?categoryCode=${category.categoryCode}${detail.cateDetailCode}" class="category-link">
                                            ${detail.cateDetailName}
                                    </a>
                                </div>
                            </c:forEach>
                        </div>
                    </c:forEach>

                </div>
            </div>
        </div>
        <div>브랜드관</div>
        <div>코스튬 특가</div>
    </div>
    <div class="header-nav nav-group-icon">
        <i class="fa-regular fa-circle-user" style="cursor:pointer;" onclick="window.location.href='/auth/login'"></i>
        <i class="fa-solid fa-magnifying-glass"></i>
        <i class="fa-solid fa-bag-shopping"></i>
    </div>
</div>

<script>
    // 헤더 스크롤 상태에 따른 클래스 추가/제거
    function updateHeaderState() {
        const header = $('.header');
        const dropdown = $('.dropdown');
        const backdrop = $('.dropdown-backdrop');

        if (window.innerWidth <= 768) {
            header.css('height', 'auto');
            header.removeClass('header-scrolled');
            // 모바일에서는 드롭다운 위치 조정 불필요
        } else if ($(window).scrollTop() > 0) {
            header.css('height', '58.5px');
            header.addClass('header-scrolled');
            // 스크롤 시 드롭다운 패딩을 줄어든 헤더에 맞춰 조정
            backdrop.css('top', '58.5px');
            backdrop.css('height', 'calc(100vh - 58.5px)');
        } else {
            header.css('height', '105.8px');
            header.removeClass('header-scrolled');
            // 기본 상태일 때 드롭다운 패딩을 원래 헤더에 맞춰 조정
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
</script>