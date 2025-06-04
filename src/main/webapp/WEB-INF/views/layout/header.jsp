<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="header">
    <div class="header-nav" onclick="movePage()" style="cursor: pointer">
        <img src="${pageContext.request.contextPath}/images/hollybam_logo.webp" width="85px;" height="27.42px;">
    </div>
    <div class="header-nav nav-group">
        <div class="nav-item">
            <div>제품</div>
            <div class="dropdown">
                <div class="dropdown-content">
                    <c:forEach var="category" items="${categories}">
                        <div class="dropdown-item">
                            <a href="/cate-page?category=${category.categoryId}" class="category-link">${category.categoryName}</a>
                        </div>
                    </c:forEach>

                </div>
            </div>
        </div>
        <div>브랜드관</div>
        <div>코스튬 특가</div>
    </div>
    <div class="header-nav nav-group-icon">
        <i class="fa-regular fa-circle-user" style="cursor:pointer;" onclick="goLogin();"></i>
        <i class="fa-solid fa-magnifying-glass"></i>
        <i class="fa-solid fa-bag-shopping"></i>
    </div>
</div>
<script>
    $(window).on('scroll resize', function () {
        if (window.innerWidth <= 768) {
            $('.header').css('height', 'auto');
        } else if ($(this).scrollTop() > 0) {
            $('.header').css('height', '58.5px');
        } else {
            $('.header').css('height', '105.8px');
        }
    });

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
</script>