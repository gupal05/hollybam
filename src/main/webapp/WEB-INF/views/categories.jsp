<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>카테고리 - 남성용품</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css"/>
    <link href="https://fonts.googleapis.com/css?family=Noto+Sans+KR:400,500,700&display=swap" rel="stylesheet">
    <script src="https://kit.fontawesome.com/69077b3f9d.js" crossorigin="anonymous"></script>
    <style>
        body {
            margin: 0;
            padding: 0;
            font-family: 'Noto Sans KR', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #ffffff;
            line-height: 1.6;
        }
        .category-padding-top {
            padding-top: 110px; /* header 기본 높이 + 약간의 여유 */
            transition: padding-top 0.3s;
        }
        @media (max-width: 700px) {
            .category-padding-top {
                padding-top: 70px;
            }
        }
        .breadcrumb {
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #888;
            margin-bottom: 18px;
            gap: 6px;
        }
        .breadcrumb a {
            color: #888;
            text-decoration: none;
            transition: color 0.2s;
        }
        .breadcrumb a:hover {
            color: #EE386D;
            text-decoration: underline;
        }
        .breadcrumb .current {
            color: #222;
            font-weight: 600;
        }
        .breadcrumb-sep {
            color: #ccc;
            margin: 0 4px;
        }
        .category-main-container {
            display: flex;
            max-width: 1280px;
            margin: 0 auto;
            padding: 0 20px 60px;
            background: transparent;
            min-height: 100vh;
            box-sizing: border-box;
        }
        .category-sidebar {
            width: 220px;
            min-width: 180px;
            margin-right: 44px;
            padding-top: 10px;
        }
        .main-category-title {
            font-size: 22px;
            font-weight: 700;
            color: #1a1a1a;
            margin-bottom: 28px;
            padding-left: 4px;
            border-left: 4px solid #EE386D;
            letter-spacing: -0.5px;
        }
        .sub-category-list {
            list-style: none;
            padding: 0;
            margin: 0;
        }
        .sub-category-item {
            font-size: 15px;
            color: #666;
            padding: 13px 0 13px 14px;
            border-radius: 8px;
            cursor: pointer;
            transition: background 0.18s, color 0.18s;
            margin-bottom: 2px;
            font-weight: 500;
            border: none;
            outline: none;
            background: none;
            text-align: left;
            width: 100%;
            display: block;
        }
        .sub-category-item.active,
        .sub-category-item:hover {
            background: linear-gradient(90deg, #fbe7ed 0%, #fff 100%);
            color: #EE386D;
            font-weight: 700;
        }
        .category-product-area {
            flex: 1;
            min-width: 0;
            display: flex;
            flex-direction: column;
        }
        .product-list-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-end;
            margin-bottom: 28px;
        }
        .product-list-title {
            font-size: 20px;
            font-weight: 600;
            color: #222;
            letter-spacing: -0.5px;
        }
        .product-sort-select {
            font-size: 15px;
            padding: 7px 16px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            background: #fff;
            color: #444;
            outline: none;
            transition: border 0.2s;
        }
        .product-sort-select:focus {
            border: 1.5px solid #EE386D;
        }
        .product-row {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
            width: 100%;
        }
        .v4-product {
            position: relative;
            background: transparent;
            border: none;
            border-radius: 0;
            box-shadow: none;
            overflow: visible;
            transition: transform 0.3s ease;
            cursor: pointer;
        }
        .v4-product:hover {
            transform: translateY(-3px);
        }
        .card-product-widget {
            display: flex;
            flex-direction: column;
            text-decoration: none;
            color: inherit;
        }
        .card-product-top-text {
            position: absolute;
            top: 10px;
            left: 10px;
            background: #EE386D;
            color: white;
            padding: 6px 10px;
            border-radius: 15px;
            font-size: 12px;
            font-weight: 700;
            z-index: 2;
        }
        .card-product-img {
            position: relative;
            background: #f5f5f5;
            border-radius: 20px;
            overflow: hidden;
            aspect-ratio: 1;
            margin-bottom: 10px;
        }
        .card-product-img img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.3s ease;
        }
        .v4-product:hover .card-product-img img {
            transform: scale(1.05);
        }
        .card-product-info {
            padding: 0;
            display: flex;
            flex-direction: column;
            gap: 8px;
        }
        .card-product-title {
            font-size: 14px;
            font-weight: 600;
            color: #333333;
            line-height: 1.4;
            margin-bottom: 5px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            min-height: 38px;
        }
        .card-product-price {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 5px;
        }
        .sale-perc {
            background: #EE386D;
            color: white;
            padding: 2px 6px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 700;
        }
        .sale_price {
            font-size: 16px;
            font-weight: 700;
            color: #000000;
            letter-spacing: -0.3px;
        }
        .regular_price {
            font-size: 12px;
            color: #888888;
            text-decoration: line-through;
        }
        .card-product-description {
            font-size: 12px;
            color: #888888;
            line-height: 1.4;
            margin-bottom: 8px;
        }
        .product_review_container {
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 11px;
            color: #888888;
            font-weight: 600;
        }
        .heart-icon, .star-icon {
            width: 12px;
            height: 12px;
            fill: #EE386D;
        }
        .star-icon {
            fill: #FFD600;
        }
        .wishlist-wrap {
            position: absolute;
            top: 10px;
            right: 10px;
            z-index: 2;
        }
        .wishlist-button {
            background: none;
            border: none;
            cursor: pointer;
            padding: 8px;
            border-radius: 50%;
            background: rgba(255,255,255,0.9);
            backdrop-filter: blur(10px);
            transition: all 0.3s ease;
        }
        .wishlist-button:hover {
            background: #EE386D;
        }
        .wishlist-button:hover .heart-empty {
            fill: white;
        }
        .heart-empty {
            width: 20px;
            height: 20px;
            fill: #888888;
            transition: fill 0.3s ease;
        }
        @media (max-width: 1024px) {
            .category-main-container {
                flex-direction: column;
                padding: 0 4vw 30px;
            }
            .category-sidebar {
                width: 100%;
                margin-right: 0;
                margin-bottom: 30px;
            }
            .product-row {
                grid-template-columns: repeat(2, 1fr);
                gap: 15px;
            }
        }
        @media (max-width: 700px) {
            .category-main-container {
                padding: 0 2vw 10px;
            }
            .product-row {
                grid-template-columns: 1fr;
                gap: 10px;
            }
            .main-category-title {
                font-size: 18px;
            }
        }
    </style>
    <script>
        // 헤더 높이에 따라 상단 여백 동적으로 조절
        function adjustCategoryPadding() {
            var el = document.querySelector('.category-padding-top');
            if (!el) return;
            // header가 축소된 상태라면
            if (window.scrollY > 10) {
                el.style.paddingTop = '62px';
            } else {
                el.style.paddingTop = '110px';
            }
        }
        window.addEventListener('scroll', adjustCategoryPadding);
        window.addEventListener('DOMContentLoaded', adjustCategoryPadding);
    </script>
</head>
<body>
<jsp:include page="layout/header.jsp" />

<div class="category-padding-top">
    <div class="category-main-container">
        <section style="flex: 1; width: 100%;">
            <!-- 브레드크럼 -->
            <nav class="breadcrumb" aria-label="breadcrumb">
                <a href="${pageContext.request.contextPath}/">홈</a>
                <span class="breadcrumb-sep">›</span>
                <a href="${pageContext.request.contextPath}/product/list?categoryCode=${cate.categoryCode}">${cate.categoryName}</a>
                <c:if test="${not empty active.detailCode}">
                    <span class="breadcrumb-sep">›</span>
                    <span class="current">
                        <c:forEach var="detail" items="${cate.categoryDetailDto}">
                            <c:if test="${detail.cateDetailCode == active.detailCode}">
                                ${detail.cateDetailName}
                            </c:if>
                        </c:forEach>
                    </span>
                </c:if>
            </nav>
            <div style="display: flex;">
                <!-- 좌측 카테고리 -->
                <aside class="category-sidebar">
                    <div class="main-category-title" style="cursor: pointer" onclick="location.href='/product/list?categoryCode=${cate.categoryCode}'">${cate.categoryName}</div>
                    <ul class="sub-category-list">
                        <c:forEach var="detail" items="${cate.categoryDetailDto}">
                            <li onclick="location.href='/product/list?categoryCode=${cate.categoryCode}${detail.cateDetailCode}'" class="${detail.cateDetailCode == active.detailCode ? 'sub-category-item active' : 'sub-category-item'}">
                                    ${detail.cateDetailName}
                            </li>
                        </c:forEach>
                    </ul>
                </aside>

                <section class="category-product-area">
                    <div class="product-list-header">
                        <span class="product-list-title">
                          ${cate.categoryName} -
                          <c:choose>
                              <c:when test="${not empty active.detailCode}">
                                  <c:forEach var="detail" items="${cate.categoryDetailDto}">
                                      <c:if test="${detail.cateDetailCode == active.detailCode}">
                                          ${detail.cateDetailName}
                                      </c:if>
                                  </c:forEach>
                              </c:when>
                              <c:otherwise>
                                  전체
                              </c:otherwise>
                          </c:choose>
                        </span>
                        <select class="product-sort-select">
                            <option value="latest">최신순</option>
                            <option value="popular">인기순</option>
                            <option value="highprice">가격높은순</option>
                            <option value="lowprice">가격낮은순</option>
                        </select>
                    </div>
                    <div class="product-row">
                        <!-- 상품 카드 예시 1 -->
                        <article class="v4-product">
                            <a class="card-product-widget" href="#">
                                <div class="card-product-img">
                                    <img src="${pageContext.request.contextPath}/images/귀엽고 섹시한 니아 165cm.jpg" alt="상품 이미지"/>
                                </div>
                                <div class="card-product-info">
                                    <p class="card-product-title">귀엽고 섹시한 니아 165cm</p>
                                    <div class="card-product-price">
                                        <div class="sale_price">₩3,500,000</div>
                                    </div>
                                    <div class="card-product-description">프리미엄 품질의 165cm 니아</div>
                                    <div class="product_review_container">
                                        <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                                        <span>32</span>
                                        <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                                        <span>4.8 (15)</span>
                                    </div>
                                </div>
                            </a>
                            <div class="wishlist-wrap">
                                <button class="wishlist-button" type="button">
                                    <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                                </button>
                            </div>
                        </article>
                        <!-- 상품 카드 예시 2 -->
                        <article class="v4-product">
                            <a class="card-product-widget" href="#">
                                <div class="card-product-img">
                                    <img src="${pageContext.request.contextPath}/images/프리미엄 리얼돌 170cm.jpg" alt="상품 이미지"/>
                                </div>
                                <div class="card-product-info">
                                    <p class="card-product-title">프리미엄 리얼돌 170cm</p>
                                    <div class="card-product-price">
                                        <div class="sale_price">₩4,200,000</div>
                                    </div>
                                    <div class="card-product-description">실감나는 피부와 디테일</div>
                                    <div class="product_review_container">
                                        <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                                        <span>21</span>
                                        <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                                        <span>4.9 (12)</span>
                                    </div>
                                </div>
                            </a>
                            <div class="wishlist-wrap">
                                <button class="wishlist-button" type="button">
                                    <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                                </button>
                            </div>
                        </article>
                        <!-- 상품 카드 예시 3 -->
                        <article class="v4-product">
                            <a class="card-product-widget" href="#">
                                <div class="card-product-img">
                                    <img src="${pageContext.request.contextPath}/images/슬림 리얼돌 155cm.jpg" alt="상품 이미지"/>
                                </div>
                                <div class="card-product-info">
                                    <p class="card-product-title">슬림 리얼돌 155cm</p>
                                    <div class="card-product-price">
                                        <div class="sale_price">₩2,950,000</div>
                                    </div>
                                    <div class="card-product-description">슬림한 바디와 가벼운 무게</div>
                                    <div class="product_review_container">
                                        <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                                        <span>12</span>
                                        <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                                        <span>4.7 (7)</span>
                                    </div>
                                </div>
                            </a>
                            <div class="wishlist-wrap">
                                <button class="wishlist-button" type="button">
                                    <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                                </button>
                            </div>
                        </article>
                        <!-- 필요시 더 추가 -->
                    </div>
                </section>
            </div>
        </section>
    </div>
</div>

<jsp:include page="layout/footer.jsp" />

<script>
</script>

</body>
</html>
