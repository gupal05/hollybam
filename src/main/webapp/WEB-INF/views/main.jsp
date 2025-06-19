<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>Sticky Header with Slide</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://kit.fontawesome.com/69077b3f9d.js" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css"/>

    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            padding: 0;
            font-family: 'Noto Sans KR', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #ffffff;
            line-height: 1.6;
        }

        /* 슬라이드 */
        .slide {
            width: 100%;
            height: 450px;
            overflow: hidden;
            position: relative;
        }
        .slide img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            position: absolute;
            top: 0;
            left: 0;
            opacity: 0;
            transition: opacity 1s ease;
            z-index: 0;
        }
        .slide img.active {
            opacity: 1;
            z-index: 1;
        }
        .dots {
            position: absolute;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%);
            display: flex;
            gap: 10px;
            z-index: 10;
        }
        .dot {
            width: 12px;
            height: 12px;
            background-color: rgba(255,255,255,0.4);
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s ease;
            backdrop-filter: blur(4px);
        }
        .dot.active {
            width: 30px;
            border-radius: 8px;
            background-color: #EE386D;
        }

        /* 카테고리 섹션 */
        .category-section {
            background: #ffffff;
            padding: 40px 20px;
            margin-top: 60px;
        }

        .category-container {
            max-width: 1280px;
            margin: 0 auto;
        }

        .category-title {
            font-size: 20px;
            font-weight: 600;
            color: #1a1a1a;
            text-align: center;
            margin-bottom: 30px;
            position: relative;
        }

        .category-grid {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 20px;
            flex-wrap: nowrap;
            overflow-x: hidden;
            padding: 0 10px;
            -ms-overflow-style: none;
            scrollbar-width: none;
        }

        .category-grid::-webkit-scrollbar {
            display: none;
        }

        .category-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            cursor: pointer;
            transition: transform 0.2s ease;
            flex-shrink: 0;
            min-width: 70px;
        }

        .category-item:hover {
            transform: translateY(-2px);
        }

        .category-icon {
            width: 55px;
            height: 55px;
            background: #ffffff;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            transition: all 0.2s ease;
            position: relative;
            overflow: hidden;
            border: 1px solid #f0f0f0;
        }

        .category-icon:hover {
            box-shadow: 0 4px 12px rgba(238, 56, 109, 0.15);
            transform: scale(1.02);
        }

        .category-icon img {
            width: 35px;
            height: 35px;
            object-fit: cover;
            border-radius: 50%;
        }

        .category-icon i {
            font-size: 22px;
            color: #EE386D;
        }

        .category-label {
            font-size: 12px;
            font-weight: 400;
            color: #666666;
            text-align: center;
            line-height: 1.2;
            white-space: nowrap;
        }

        .category-badge {
            position: absolute;
            top: -3px;
            right: -3px;
            background: #EE386D;
            color: white;
            font-size: 8px;
            font-weight: 600;
            padding: 1px 4px;
            border-radius: 6px;
            z-index: 2;
        }

        /* 콘텐츠 */
        .content {
            min-height: 800px;
            display: flex;
            justify-content: center;
            padding: 60px 20px 0;
        }
        .inner-box {
            display: flex;
            flex-direction: column;
            width: 100%;
            max-width: 1280px;
        }

        .section-title {
            margin-bottom: 32px;
            font-size: 26px;
            font-weight: 600;
            color: #1a1a1a;
            text-align: left;
            position: relative;
            padding-left: 0;
            letter-spacing: -0.4px;
            line-height: 1.3;
        }

        .section-title::after {
            content: '';
            position: absolute;
            bottom: -6px;
            left: 0;
            width: 40px;
            height: 2px;
            background: #EE386D;
            border-radius: 1px;
        }

        /* 업데이트된 상품 카드 디자인 */
        .product-row {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            width: 100%;
        }

        .v4-product {
            position: relative;
            background: transparent; /* 배경 투명하게 */
            border: none; /* 테두리 제거 */
            border-radius: 0; /* 카드 테두리 곡선 제거 */
            box-shadow: none; /* 그림자 제거 */
            overflow: visible;
            transition: transform 0.3s ease;
            cursor: pointer;
        }

        .v4-product:hover {
            transform: translateY(-3px); /* 호버 효과 살짝 줄임 */
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
            border-radius: 20px; /* 상하좌우 모든 모서리 둥글게 */
            overflow: hidden;
            aspect-ratio: 1;
            margin-bottom: 10px; /* 이미지와 텍스트 사이 간격 */
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
            padding: 0; /* 패딩 제거 */
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

        .sr-shipping-icon {
            width: 60px;
            height: auto;
            margin-top: 5px;
        }

        .product-img {
            display: block;
            width: 100%;
            height: auto;
            transition: opacity 0.4s ease-in-out;
        }

        /* 홀리밤 인스타 툰 섹션 */
        .instagramtoon-section {
            margin-top: 120px; /* 기존 60px에서 120px로 증가 */
            width: 100%;
            position: relative;
        }

        /* 섹션 구분을 위한 subtle한 구분선 추가 */
        .instagramtoon-section::before {
            content: '';
            position: absolute;
            top: -60px;
            left: 50%;
            transform: translateX(-50%);
            width: 100px;
            height: 1px;
            background: linear-gradient(to right, transparent, #e0e0e0, transparent);
        }

        .instagramtoon-container {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 100%;
            height: 700px;
            background: #f8f9fa;
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            margin-top: 20px; /* 타이틀과 컨테이너 사이 간격 추가 */
        }

        .instagramtoon-placeholder {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-align: center;
        }

        .instagramtoon-placeholder h3 {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 10px;
        }

        .instagramtoon-placeholder p {
            font-size: 14px;
            opacity: 0.8;
        }

        /* 유튜브 iframe 스타일 (나중에 사용) */
        .youtube-iframe {
            width: 100%;
            height: 100%;
            border: none;
            border-radius: 20px;
        }

        /* 반응형 */
        @media (max-width: 1024px) {
            .category-grid {
                gap: 15px;
            }
            .category-icon {
                width: 50px;
                height: 50px;
            }
            .category-icon img {
                width: 32px;
                height: 32px;
            }
            .category-icon i {
                font-size: 20px;
            }
            .product-row {
                grid-template-columns: repeat(3, 1fr);
                gap: 15px;
            }
            .section-title {
                font-size: 24px;
                margin-bottom: 24px;
            }
            .instagramtoon-section {
                margin-top: 100px;
            }
            .instagramtoon-section::before {
                top: -50px;
                width: 80px;
            }
        }

        @media (max-width: 768px) {
            .category-section {
                padding: 30px 16px;
                margin-top: 480px;
            }
            .category-grid {
                gap: 12px;
                padding: 0 5px;
            }
            .category-item {
                min-width: 60px;
            }
            .category-icon {
                width: 45px;
                height: 45px;
            }
            .category-icon img {
                width: 28px;
                height: 28px;
            }
            .category-icon i {
                font-size: 18px;
            }
            .category-label {
                font-size: 11px;
            }
            .content {
                padding: 40px 16px 0;
            }
            .product-row {
                grid-template-columns: repeat(2, 1fr);
                gap: 15px;
            }
            .section-title {
                font-size: 22px;
                margin-bottom: 20px;
            }
            .instagramtoon-section {
                margin-top: 80px;
            }
            .instagramtoon-section::before {
                top: -40px;
                width: 60px;
            }
            .instagramtoon-container {
                height: 600px;
                border-radius: 15px;
                margin-top: 16px;
            }
            .instagramtoon-placeholder h3 {
                font-size: 20px;
            }
            .instagramtoon-placeholder p {
                font-size: 12px;
            }
        }

        @media (max-width: 480px) {
            .category-grid {
                gap: 8px;
                padding: 0;
            }
            .category-item {
                min-width: 50px;
            }
            .category-icon {
                width: 40px;
                height: 40px;
            }
            .category-icon img {
                width: 25px;
                height: 25px;
            }
            .category-icon i {
                font-size: 16px;
            }
            .category-label {
                font-size: 10px;
            }
            .product-row {
                grid-template-columns: repeat(2, 1fr);
                gap: 10px;
            }
            .content {
                padding: 30px 12px 0;
            }
            .section-title {
                font-size: 20px;
                margin-bottom: 16px;
            }
            .instagramtoon-section {
                margin-top: 60px;
            }
            .instagramtoon-section::before {
                display: none; /* 모바일에서는 구분선 숨김 */
            }
            .instagramtoon-container {
                height: 500px;
                border-radius: 12px;
                margin-top: 12px;
            }
            .instagramtoon-placeholder h3 {
                font-size: 18px;
            }
        }
    </style>
</head>
<body>
<%-- 고정 헤더 --%>
<jsp:include page="layout/header.jsp"></jsp:include>
<%-- 슬라이드 --%>
<div class="slide"> <!-- header style2 = top 80px -->
    <img src="${pageContext.request.contextPath}/images/da1589be137666f771fcb06bd045c8ff.webp" alt="슬라이드1" class="active"/>
    <img src="${pageContext.request.contextPath}/images/24e86dfad799d3ffad7d032aa0f9fb68.webp" alt="슬라이드2"/>
    <div class="dots">
        <div class="dot active"></div>
        <div class="dot"></div>
    </div>
</div>



<%-- 카테고리 섹션 --%>
<div class="category-section">
    <div class="category-container">
        <div class="category-grid">
            <div class="category-item">
                <div class="category-icon">
                    <span class="category-badge">HOT</span>
                    <i class="fas fa-heart"></i>
                </div>
                <div class="category-label">인기브랜드</div>
            </div>
            <div class="category-item">
                <div class="category-icon">
                    <span class="category-badge">New</span>
                    <i class="fas fa-user"></i>
                </div>
                <div class="category-label">MD픽</div>
            </div>
            <div class="category-item">
                <div class="category-icon">
                    <i class="fas fa-gift"></i>
                </div>
                <div class="category-label">첫날밤 용품</div>
            </div>
            <div class="category-item">
                <div class="category-icon">
                    <i class="fas fa-star"></i>
                </div>
                <div class="category-label">첫날밤 약</div>
            </div>
            <div class="category-item">
                <div class="category-icon">
                    <i class="fas fa-laptop"></i>
                </div>
                <div class="category-label">건강용품</div>
            </div>
            <div class="category-item">
                <div class="category-icon">
                    <i class="fas fa-dumbbell"></i>
                </div>
                <div class="category-label">헬스케어</div>
            </div>
            <div class="category-item">
                <div class="category-icon">
                    <i class="fas fa-cube"></i>
                </div>
                <div class="category-label">기념품</div>
            </div>
            <div class="category-item">
                <div class="category-icon">
                    <i class="fas fa-female"></i>
                </div>
                <div class="category-label">여성케어</div>
            </div>
        </div>
    </div>
</div>

<%-- 메인 콘텐츠 --%>
<div class="content">
    <div class="inner-box">
        <div class="section-title">베스트 상품</div>
        <div class="product-row">
            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-top-text">HOT🔥</div>
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀리밤 피스톤 머시 (10종 구성).png"
                             data-hover="${pageContext.request.contextPath}/images/홀리밤 피스톤 머시 (10종 구성).webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀리밤 피스톤 머신</p>
                        <div class="card-product-price">
                            <div class="sale-perc">25%</div>
                            <div class="sale_price">₩150,000</div>
                            <div class="regular_price">₩200,000</div>
                        </div>
                        <div class="card-product-description">프리미엄 품질의 홀리밤 피스톤 머신 10종 구성</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>45</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.9 (23)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>

            <article class="v4-product">
                <a class="card-product-widget" href="/product/productDetail">
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

            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-top-text">NEW✨</div>
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀 래빗 진동 흡입기.png"
                             data-hover="${pageContext.request.contextPath}/images/홀 래빗 진동 흡입기.webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀 래빗 진동 흡입기</p>
                        <div class="card-product-price">
                            <div class="sale-perc">26%</div>
                            <div class="sale_price">₩89,000</div>
                            <div class="regular_price">₩120,000</div>
                        </div>
                        <div class="card-product-description">고급스러운 홀 래빗 진동 흡입기</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>28</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.7 (18)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>
            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀리밤 페어리 딜도.png"
                             data-hover="${pageContext.request.contextPath}/images/홀리밤 페어리 딜도.webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀리밤 페어리 딜도</p>
                        <div class="card-product-price">
                            <div class="sale-perc">92%</div>
                            <div class="sale_price">₩79,000</div>
                            <div class="regular_price">₩1,000,000</div>
                        </div>
                        <div class="card-product-description">프리미엄 품질의 홀리밤 페어리 딜도</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>45</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.9 (23)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>
            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-top-text">HOT🔥</div>
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀리밤 피스톤 머시 (10종 구성).png"
                             data-hover="${pageContext.request.contextPath}/images/홀리밤 피스톤 머시 (10종 구성).webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀리밤 피스톤 머신</p>
                        <div class="card-product-price">
                            <div class="sale-perc">25%</div>
                            <div class="sale_price">₩150,000</div>
                            <div class="regular_price">₩200,000</div>
                        </div>
                        <div class="card-product-description">프리미엄 품질의 홀리밤 피스톤 머신 10종 구성</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>45</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.9 (23)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>

            <article class="v4-product">
                <a class="card-product-widget" href="/product/productDetail">
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

            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-top-text">NEW✨</div>
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀 래빗 진동 흡입기.png"
                             data-hover="${pageContext.request.contextPath}/images/홀 래빗 진동 흡입기.webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀 래빗 진동 흡입기</p>
                        <div class="card-product-price">
                            <div class="sale-perc">26%</div>
                            <div class="sale_price">₩89,000</div>
                            <div class="regular_price">₩120,000</div>
                        </div>
                        <div class="card-product-description">고급스러운 홀 래빗 진동 흡입기</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>28</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.7 (18)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>
            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀리밤 페어리 딜도.png"
                             data-hover="${pageContext.request.contextPath}/images/홀리밤 페어리 딜도.webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀리밤 페어리 딜도</p>
                        <div class="card-product-price">
                            <div class="sale-perc">92%</div>
                            <div class="sale_price">₩79,000</div>
                            <div class="regular_price">₩1,000,000</div>
                        </div>
                        <div class="card-product-description">프리미엄 품질의 홀리밤 페어리 딜도</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>45</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.9 (23)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>
        </div>
        <div class="instagramtoon-section">
            <div class="section-title">홀리밤 인스타 툰</div>
            <div class="instagramtoon-container">
                <!-- 현재는 플레이스홀더, 나중에 iframe으로 교체 -->
                <div class="instagramtoon-placeholder">
                    <h3>홀리밤 인스타 툰</h3>
                    <p>유튜브 영상이 여기에 표시됩니다</p>
                </div>

                <!-- 나중에 유튜브 연결 시 아래 iframe 주석 해제하고 위의 placeholder div 삭제 -->
                <!--
                <iframe
                    class="youtube-iframe"
                    src="https://www.youtube.com/embed/videoseries?si=E-736Jojxc_7Dm5L&list=PLFZwuSDJTOCqSd1PFcBidLXtIBnX6bzCu"
                    title="홀리밤 인스타 툰"
                    frameborder="0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                    referrerpolicy="strict-origin-when-cross-origin"
                    allowfullscreen>
                </iframe>
                -->
            </div>
        </div>
        <div class="section-title" style="margin-top: 120px;">신제품</div>
        <div class="product-row">
            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-top-text">HOT🔥</div>
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀리밤 피스톤 머시 (10종 구성).png"
                             data-hover="${pageContext.request.contextPath}/images/홀리밤 피스톤 머시 (10종 구성).webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀리밤 피스톤 머신</p>
                        <div class="card-product-price">
                            <div class="sale-perc">25%</div>
                            <div class="sale_price">₩150,000</div>
                            <div class="regular_price">₩200,000</div>
                        </div>
                        <div class="card-product-description">프리미엄 품질의 홀리밤 피스톤 머신 10종 구성</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>45</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.9 (23)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>

            <article class="v4-product">
                <a class="card-product-widget" href="/product/productDetail">
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

            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-top-text">NEW✨</div>
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀 래빗 진동 흡입기.png"
                             data-hover="${pageContext.request.contextPath}/images/홀 래빗 진동 흡입기.webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀 래빗 진동 흡입기</p>
                        <div class="card-product-price">
                            <div class="sale-perc">26%</div>
                            <div class="sale_price">₩89,000</div>
                            <div class="regular_price">₩120,000</div>
                        </div>
                        <div class="card-product-description">고급스러운 홀 래빗 진동 흡입기</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>28</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.7 (18)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>
            <article class="v4-product">
                <a class="card-product-widget" href="#">
                    <div class="card-product-img">
                        <img class="product-img"
                             src="${pageContext.request.contextPath}/images/홀리밤 페어리 딜도.png"
                             data-hover="${pageContext.request.contextPath}/images/홀리밤 페어리 딜도.webp"
                             alt="상품 이미지" />
                    </div>
                    <div class="card-product-info">
                        <p class="card-product-title">홀리밤 페어리 딜도</p>
                        <div class="card-product-price">
                            <div class="sale-perc">92%</div>
                            <div class="sale_price">₩79,000</div>
                            <div class="regular_price">₩1,000,000</div>
                        </div>
                        <div class="card-product-description">프리미엄 품질의 홀리밤 페어리 딜도</div>
                        <div class="product_review_container">
                            <svg class="heart-icon" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
                            <span>45</span>
                            <svg class="star-icon" viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                            <span>4.9 (23)</span>
                        </div>
                    </div>
                </a>
                <div class="wishlist-wrap">
                    <button class="wishlist-button" type="button">
                        <svg class="heart-empty" viewBox="0 0 24 24"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z"/></svg>
                    </button>
                </div>
            </article>
        </div>
    </div>
</div>
<jsp:include page="layout/footer.jsp"></jsp:include>
<script>
    let current = 0;
    const images = $('.slide img');
    const dots = $('.dot');
    const changeSlide = (index) => {
        images.removeClass('active').eq(index).addClass('active');
        dots.removeClass('active').eq(index).addClass('active');
        current = index;
    };

    dots.each((i, el) => {
        $(el).on('click', () => {
            changeSlide(i);
        });
    });

    setInterval(() => {
        const next = (current + 1) % images.length;
        changeSlide(next);
    }, 8000);

    // 카테고리 클릭 이벤트
    $('.category-item').on('click', function() {
        const categoryName = $(this).find('.category-label').text();
        console.log('카테고리 클릭:', categoryName);
        // 여기에 카테고리 페이지로 이동하는 로직을 추가할 수 있습니다
    });

    document.addEventListener("DOMContentLoaded", function () {
        const imgs = document.querySelectorAll(".product-img");

        imgs.forEach(img => {
            const originalSrc = img.getAttribute("src");
            const hoverSrc = img.getAttribute("data-hover");

            img.addEventListener("mouseenter", () => {
                img.setAttribute("src", hoverSrc);
            });

            img.addEventListener("mouseleave", () => {
                img.setAttribute("src", originalSrc);
            });
        });
    });
</script>
</body>
</html>