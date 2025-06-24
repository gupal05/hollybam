// 홀리밤 소식 슬라이더 - 완전 수정된 반응형 버전
$(document).ready(function() {
    const $newsSlider = $('#newsSlider');
    const $newsCards = $('.news-card');
    const $newsDots = $('.news-dot');
    const newsCardCount = $newsCards.length;

    if (newsCardCount > 0) {
        let newsCurrentIndex = 0;
        let newsSlideDistance = 448; // 기본 슬라이드 거리
        let newsCurrentPosition = 0;
        let newsIsTransitioning = false;
        let newsVisibleCards = 3;
        let isDesktop = true;

        // 반응형 처리 - 더 정확한 breakpoint 체크
        function updateNewsSliderSettings() {
            const windowWidth = $(window).width();
            const wasDesktop = isDesktop;

            if (windowWidth <= 1024) {
                // 태블릿 및 모바일: 한 장씩 표시
                newsVisibleCards = 1;
                isDesktop = false;

                // 실제 컨테이너 너비 사용 (패딩 없이 전체 너비)
                const containerWidth = $newsSlider.parent().outerWidth();
                newsSlideDistance = containerWidth;
            } else {
                // 데스크톱: 3장씩 표시 (기존 유지)
                newsVisibleCards = 3;
                newsSlideDistance = 448; // 원본 값 유지
                isDesktop = true;
            }

            // 데스크톱 ↔ 모바일 전환 시 슬라이더 재설정
            if (wasDesktop !== isDesktop) {
                return true; // 재설정 필요
            }
            return false; // 재설정 불필요
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
                    $newsSlider.css('transition', 'none');
                    newsCurrentPosition = newsCardCount * newsSlideDistance;
                    $newsSlider.css('transform', 'translateX(' + (-newsCurrentPosition) + 'px)');
                }
                // 시작에 도달했을 때 처리
                else if (newsCurrentPosition < newsCardCount * newsSlideDistance) {
                    $newsSlider.css('transition', 'none');
                    newsCurrentPosition = (newsCardCount * 2 - 1) * newsSlideDistance;
                    $newsSlider.css('transform', 'translateX(' + (-newsCurrentPosition) + 'px)');
                }

                // 다음 프레임에서 transition 재활성화
                requestAnimationFrame(() => {
                    $newsSlider.css('transition', 'transform 0.8s ease');
                    newsIsTransitioning = false;
                });
            }, 800);
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

        // 터치/스와이프 이벤트
        let startX = 0;
        let isDragging = false;
        let hasTouchEvents = false;

        function addTouchEvents() {
            if (hasTouchEvents) return;
            hasTouchEvents = true;

            $newsSlider.on('touchstart.newsSlider', function(e) {
                if (newsIsTransitioning) return;
                isDragging = true;
                startX = e.originalEvent.touches[0].clientX;
                e.preventDefault();
            });

            $newsSlider.on('touchmove.newsSlider', function(e) {
                if (!isDragging) return;
                e.preventDefault();
            });

            $newsSlider.on('touchend.newsSlider', function(e) {
                if (!isDragging) return;
                isDragging = false;

                const endX = e.originalEvent.changedTouches[0].clientX;
                const diffX = startX - endX;

                // 50px 이상 움직였을 때만 슬라이드 변경
                if (Math.abs(diffX) > 50) {
                    stopNewsAutoSlide();
                    if (diffX > 0) {
                        moveNewsSlider('next');
                    } else {
                        moveNewsSlider('prev');
                    }
                    setTimeout(startNewsAutoSlide, 1000);
                }
            });
        }

        function removeTouchEvents() {
            $newsSlider.off('.newsSlider');
            hasTouchEvents = false;
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
            setTimeout(startNewsAutoSlide, 1000);
        });

        $('#newsPrevBtn').on('click', () => {
            moveNewsSlider('prev');
            stopNewsAutoSlide();
            setTimeout(startNewsAutoSlide, 1000);
        });

        $newsDots.on('click', function() {
            const slideIndex = parseInt($(this).data('slide'));
            goToNewsSlide(slideIndex);
            stopNewsAutoSlide();
            setTimeout(startNewsAutoSlide, 1000);
        });

        $('.hollybam-news-section').hover(stopNewsAutoSlide, startNewsAutoSlide);

        // 리사이즈 이벤트 - 디바운스 추가
        let resizeTimer;
        $(window).on('resize', function() {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(() => {
                const needsReset = updateNewsSliderSettings();

                if (needsReset) {
                    stopNewsAutoSlide();

                    // 슬라이더 재설정
                    setupNewsInfiniteLoop();
                    newsCurrentIndex = 0;
                    updateNewsDots();

                    // 터치 이벤트 재설정
                    removeTouchEvents();
                    if (!isDesktop) {
                        addTouchEvents();
                    }

                    setTimeout(startNewsAutoSlide, 500);
                } else {
                    // 같은 모드 내에서의 크기 변경은 슬라이드 거리만 업데이트
                    if (!isDesktop) {
                        const containerWidth = $newsSlider.parent().outerWidth();
                        const newDistance = containerWidth; // 패딩 없이 전체 너비 사용

                        if (Math.abs(newDistance - newsSlideDistance) > 10) {
                            newsSlideDistance = newDistance;
                            newsCurrentPosition = (newsCardCount + newsCurrentIndex) * newsSlideDistance;
                            $newsSlider.css({
                                'transition': 'none',
                                'transform': 'translateX(' + (-newsCurrentPosition) + 'px)'
                            });
                        }
                    }
                }
            }, 250);
        });

        // 초기 설정
        updateNewsSliderSettings();
        setupNewsInfiniteLoop();
        updateNewsDots();

        // 모바일/태블릿에서만 터치 이벤트 추가
        if (!isDesktop) {
            addTouchEvents();
        }

        startNewsAutoSlide();
    }
});

// 비디오 팝업 기능 (기존 유지)
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