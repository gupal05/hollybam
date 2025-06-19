// 홀리밤 소식 슬라이더
$(document).ready(function() {
    const $newsSlider = $('#newsSlider');
    const $newsCards = $('.news-card');
    const $newsDots = $('.news-dot');
    const newsCardCount = $newsCards.length;

    if (newsCardCount > 0) {
        let newsCurrentIndex = 0;
        let newsSlideDistance = 448; // 고정된 슬라이드 거리 448px
        let newsCurrentPosition = 0;
        let newsIsTransitioning = false;
        let newsVisibleCards = 3;

        // 반응형 처리
        function updateNewsSliderSettings() {
            const windowWidth = $(window).width();

            if (windowWidth <= 480) {
                newsVisibleCards = 1;
                newsSlideDistance = 320; // 모바일에서는 320px
            } else if (windowWidth <= 768) {
                newsVisibleCards = 1;
                newsSlideDistance = 360; // 태블릿에서는 360px
            } else if (windowWidth <= 1024) {
                newsVisibleCards = 2;
                newsSlideDistance = 400; // 중간 화면에서는 400px
            } else {
                newsVisibleCards = 3;
                newsSlideDistance = 448; // 데스크톱에서는 448px
            }
        }

        function setupNewsInfiniteLoop() {
            // 기존 복제 요소들 제거
            $newsSlider.find('.news-card').slice(newsCardCount).remove();

            // 무한 루프를 위한 복제
            $newsCards.each(function() {
                $newsSlider.append($(this).clone());
            });

            $newsCards.each(function() {
                $newsSlider.append($(this).clone());
            });

            // 초기 위치 설정 (중앙 섹션으로)
            newsCurrentPosition = newsCardCount * newsSlideDistance;
            $newsSlider.css({
                'transition': 'none',
                'transform': 'translateX(' + (-newsCurrentPosition) + 'px)'
            });
        }

        function updateNewsDots() {
            $newsDots.removeClass('active');
            $newsDots.eq(newsCurrentIndex).addClass('active');
        }

        function moveNewsSlider(direction = 'next') {
            if (newsIsTransitioning) return;

            newsIsTransitioning = true;

            if (direction === 'next') {
                newsCurrentPosition += newsSlideDistance;
                newsCurrentIndex = (newsCurrentIndex + 1) % newsCardCount;
            } else {
                newsCurrentPosition -= newsSlideDistance;
                newsCurrentIndex = (newsCurrentIndex - 1 + newsCardCount) % newsCardCount;
            }

            // 항상 일정한 transition 적용
            $newsSlider.css({
                'transition': 'transform 0.8s ease',
                'transform': 'translateX(' + (-newsCurrentPosition) + 'px)'
            });

            updateNewsDots();

            // transition 완료 후 무한 루프 처리
            setTimeout(() => {
                // 끝에 도달했을 때 처리
                if (newsCurrentPosition >= (newsCardCount * 2) * newsSlideDistance) {
                    // transition 없이 시작 위치로 이동
                    $newsSlider.css('transition', 'none');
                    newsCurrentPosition = newsCardCount * newsSlideDistance;
                    $newsSlider.css('transform', 'translateX(' + (-newsCurrentPosition) + 'px)');

                    // 다음 프레임에서 transition 재활성화
                    requestAnimationFrame(() => {
                        $newsSlider.css('transition', 'transform 0.8s ease');
                    });
                }
                // 시작에 도달했을 때 처리
                else if (newsCurrentPosition < newsCardCount * newsSlideDistance) {
                    // transition 없이 끝 위치로 이동
                    $newsSlider.css('transition', 'none');
                    newsCurrentPosition = (newsCardCount * 2 - 1) * newsSlideDistance;
                    $newsSlider.css('transform', 'translateX(' + (-newsCurrentPosition) + 'px)');

                    // 다음 프레임에서 transition 재활성화
                    requestAnimationFrame(() => {
                        $newsSlider.css('transition', 'transform 0.8s ease');
                    });
                }

                newsIsTransitioning = false;
            }, 800); // transition 시간과 동일하게 설정
        }

        function goToNewsSlide(index) {
            if (newsIsTransitioning) return;

            newsIsTransitioning = true;

            const targetPosition = (newsCardCount + index) * newsSlideDistance;

            newsCurrentIndex = index;
            newsCurrentPosition = targetPosition;

            $newsSlider.css({
                'transition': 'transform 0.8s ease',
                'transform': 'translateX(' + (-newsCurrentPosition) + 'px)'
            });

            updateNewsDots();

            setTimeout(() => {
                newsIsTransitioning = false;
            }, 800);
        }

        let newsAutoSlideInterval;
        function startNewsAutoSlide() {
            newsAutoSlideInterval = setInterval(() => moveNewsSlider('next'), 4000);
        }

        function stopNewsAutoSlide() {
            clearInterval(newsAutoSlideInterval);
        }

        // 이벤트 리스너
        $('#newsNextBtn').on('click', () => {
            moveNewsSlider('next');
            stopNewsAutoSlide();
            startNewsAutoSlide();
        });

        $('#newsPrevBtn').on('click', () => {
            moveNewsSlider('prev');
            stopNewsAutoSlide();
            startNewsAutoSlide();
        });

        $newsDots.on('click', function() {
            const slideIndex = parseInt($(this).data('slide'));
            goToNewsSlide(slideIndex);
            stopNewsAutoSlide();
            startNewsAutoSlide();
        });

        $('.hollybam-news-section').hover(stopNewsAutoSlide, startNewsAutoSlide);

        // 리사이즈 이벤트
        $(window).on('resize', function() {
            updateNewsSliderSettings();
            stopNewsAutoSlide();

            // 슬라이더 재설정
            setupNewsInfiniteLoop();
            newsCurrentIndex = 0;
            updateNewsDots();

            startNewsAutoSlide();
        });

        // 초기 설정
        updateNewsSliderSettings();
        setupNewsInfiniteLoop();
        updateNewsDots();
        startNewsAutoSlide();
    }
});

// 비디오 팝업 기능
$(document).ready(function() {
    const $videoModal = $('#videoModal');
    const $modalVideo = $('#modalVideo');
    const $videoClose = $('.video-close');

    // 비디오 카드 클릭 이벤트 (이벤트 위임 사용)
    $(document).on('click', '.news-card[data-video]', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const videoSrc = $(this).data('video');
        console.log('Video source:', videoSrc); // 디버깅용

        // 기존 비디오 정지
        $modalVideo[0].pause();
        $modalVideo[0].currentTime = 0;

        // 비디오 소스 설정
        $modalVideo.find('source').attr('src', videoSrc);
        $modalVideo[0].load();

        // 모달 표시
        $videoModal.fadeIn(300);

        // 비디오 로드 완료 후 재생
        $modalVideo.on('loadeddata', function() {
            this.play().catch(function(error) {
                console.error('Auto-play failed:', error);
            });
        });
    });

    // 닫기 버튼 클릭
    $videoClose.on('click', function() {
        closeVideoModal();
    });

    // 모달 배경 클릭 시 닫기
    $videoModal.on('click', function(e) {
        if (e.target === this) {
            closeVideoModal();
        }
    });

    // ESC 키로 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $videoModal.is(':visible')) {
            closeVideoModal();
        }
    });

    function closeVideoModal() {
        $modalVideo[0].pause();
        $modalVideo[0].currentTime = 0;
        $modalVideo.off('loadeddata'); // 이벤트 리스너 제거
        $videoModal.fadeOut(300);
    }
});