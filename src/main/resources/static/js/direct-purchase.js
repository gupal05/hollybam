// 서버에서 전달받은 데이터
var isDirect = /*[[${isDirect}]]*/ false;
const paymentInfo = /*[[${paymentInfo}]]*/ {};
const cartCodes = /*[[${cartCodes}]]*/ [];
const cartItems = /*[[${cartItems}]]*/ [];

// 회원/비회원 정보 (수정된 버전)
const memberInfo = {
    memberZip: /*[[${member?.memberZip ?: ''}]]*/ '',
    memberAddr: /*[[${member?.memberAddr ?: ''}]]*/ ''
};

const guestInfo = {
    guestName: /*[[${guest?.guestName ?: ''}]]*/ '',
    guestPhone: /*[[${guest?.guestPhone ?: ''}]]*/ ''
};

const hasUserInfo = /*[[${member != null or guest != null}]]*/ false;

// 할인 관련 변수 (단독 사용)
let activeDiscount = null; // 현재 활성화된 할인 (쿠폰 또는 할인코드)
let originalAmount = paymentInfo.totalAmount || 0;
let currentTotalAmount = paymentInfo.finalAmount || 0;

// 🆕 적립금 관련 변수들 - 기존 JavaScript에 추가
let currentUsePoints = 0;
let currentPoints = 0;

// 유효성 검사 상태 추적
const validationState = {
    ordererName: false,
    ordererPhone: false,
    ordererEmail: false,
    receiverName: false,
    receiverPhone: false,
    receiverAddr: false,
    agreeTerms: false,
    agreePrivacy: false,
    agreeAdult: false
};

$(document).ready(function() {
    // 이벤트 리스너 등록
    setupEventListeners();

    // 초기 유효성 검사
    validateAllFields();

    // 초기 주소 필드 상태 설정
    updateAddressFields();

    // 🆕 적립금 초기화 (회원인 경우만)
    if ($('#currentPoints').length > 0) {
        loadCurrentPoints();
    }

    // 🆕 초기 적립 예정 금액 계산
    updateRewardAmount();

    console.log("Payment Info:", paymentInfo);
    console.log("Cart Items:", cartItems);
});

// 🆕 HTML에 최대 할인 금액 안내 추가를 위한 CSS
const additionalCSS = `
.discount-limit-info {
    font-size: 12px;
    color: #666;
    background: rgba(238, 56, 109, 0.1);
    padding: 8px 12px;
    border-radius: 6px;
    border: 1px solid rgba(238, 56, 109, 0.2);
    margin-top: 8px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.discount-limit-info i {
    color: #EE386D;
}
`;

// CSS 추가
const style = document.createElement('style');
style.textContent = additionalCSS;
document.head.appendChild(style);

// 🆕 초기 로드 시 최대 할인 금액 안내 표시
$(document).ready(function() {
    // 할인 섹션에 최대 할인 금액 안내 추가
    if ($('.discount-section').length > 0) {
        $('.discount-section .form-section').first().after(`
            <div class="discount-limit-info">
                <i class="fas fa-info-circle"></i>
                <span>쿠폰 및 할인코드 최대 할인 금액: ${MAX_DISCOUNT_AMOUNT.toLocaleString()}원</span>
            </div>
        `);
    }
});

// 🆕 적립금 관련 함수들 - 기존 JavaScript 마지막에 추가
function loadCurrentPoints() {
    $.ajax({
        url: '/order/api/points',
        type: 'GET',
        success: function(response) {
            if (response.success) {
                currentPoints = response.currentPoints;
                updatePointsDisplay();
                updateRewardAmount();
            } else {
                console.error('적립금 조회 실패:', response.message);
            }
        },
        error: function() {
            console.error('적립금 조회 중 오류 발생');
        }
    });
}

function updatePointsDisplay() {
    $('#currentPoints').text(formatCurrency(currentPoints) + '원');
    $('#usePointsInput').attr('max', currentPoints);

    if (currentPoints <= 0) {
        $('#usePointsInput').prop('disabled', true);
        $('#useAllPointsBtn').prop('disabled', true);
        $('#applyPointsBtn').prop('disabled', true);
    }
}

function updateRewardAmount() {
    const rewardPoints = Math.floor(originalAmount * 0.015);

    // 2) 화면에 찍어주기 (적립 예상 영역과 요약 영역 모두)
    $('#rewardAmount').text(formatCurrency(rewardPoints) + '원 (1.5%)');
    $('#summaryRewardAmount').text(formatCurrency(rewardPoints) + '원');
}

// 최종 금액 계산 (기존 함수들과 연동)
function calculateCurrentFinalAmount() {
    const baseAmount = originalAmount || 0;
    const discountAmount = activeDiscount ? activeDiscount.amount : 0;
    const deliveryFee = paymentInfo.deliveryFee || 0;

    return Math.max(0, baseAmount - discountAmount - currentUsePoints + deliveryFee);
}

// 적립금 버튼 이벤트
$('#useAllPointsBtn').click(function() {
    if (currentPoints > 0) {
        const totalAmount = originalAmount || 0;
        const maxUsable = Math.min(currentPoints, totalAmount);
        $('#usePointsInput').val(maxUsable);
        applyPoints();
    }
});

$('#applyPointsBtn').click(function() {
    applyPoints();
});

$('#usePointsInput').on('input', function() {
    const inputValue = parseInt($(this).val()) || 0;
    const totalAmount = originalAmount || 0;
    const maxUsable = Math.min(currentPoints, totalAmount);

    if (inputValue > maxUsable) {
        $(this).val(maxUsable);
    }

    if (inputValue < 0) {
        $(this).val(0);
    }
});

function applyPoints() {
    const inputPoints = parseInt($('#usePointsInput').val()) || 0;

    if (inputPoints <= 0) {
        resetPointsUsage();
        return;
    }

    if (inputPoints > currentPoints) {
        alert('보유 적립금을 초과할 수 없습니다.');
        $('#usePointsInput').val(currentPoints);
        return;
    }

    const totalAmount = originalAmount || 0;
    if (inputPoints > totalAmount) {
        alert('주문 금액을 초과해서 사용할 수 없습니다.');
        $('#usePointsInput').val(totalAmount);
        return;
    }

    // 서버 검증
    $.ajax({
        url: '/order/api/points/validate',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ usePoints: inputPoints }),
        success: function(response) {
            if (response.success && response.available) {
                currentUsePoints = inputPoints;
                updatePointsResult();
                updatePointsOrderSummary();
                updateRewardAmount();
            } else {
                alert(response.message || '적립금 사용이 불가능합니다.');
                resetPointsUsage();
            }
        },
        error: function() {
            alert('적립금 검증 중 오류가 발생했습니다.');
            resetPointsUsage();
        }
    });
}

function updatePointsResult() {
    if (currentUsePoints > 0) {
        $('#useAmount').text(formatCurrency(currentUsePoints) + '원');
        $('#remainAmount').text(formatCurrency(currentPoints - currentUsePoints) + '원');
        $('#pointsResult').show();
    } else {
        $('#pointsResult').hide();
    }
}

function resetPointsUsage() {
    currentUsePoints = 0;
    $('#usePointsInput').val(0);
    $('#pointsResult').hide();
    updatePointsOrderSummary();
    updateRewardAmount();
}

function updatePointsOrderSummary() {
    // 적립금 할인 표시
    if (currentUsePoints > 0) {
        $('#pointsDiscountAmount').text('-₩' + formatCurrency(currentUsePoints));
        $('.points-discount-row').show();
    } else {
        $('.points-discount-row').hide();
    }

    // 최종 금액 업데이트 (기존 함수가 있다면 활용)
    const finalAmount = calculateCurrentFinalAmount();
    $('#finalAmount').text('₩' + formatCurrency(finalAmount));

    // 결제 버튼 업데이트
    $('#paymentButtonText').text('₩' + formatCurrency(finalAmount) + ' 결제하기');

    // currentTotalAmount 업데이트
    currentTotalAmount = finalAmount;
}

// 숫자 포맷팅 함수 (기존에 없다면 추가)
function formatCurrency(amount) {
    return parseInt(amount).toLocaleString();
}

// 기존 주문 데이터 수집 함수에 적립금 정보 추가하는 함수
function addPointsToOrderData(orderData) {
    if (typeof orderData === 'object') {
        orderData.usePoints = currentUsePoints;
    }
    return orderData;
}

function setupEventListeners() {
    // 결제 방법 선택
    $('.payment-method').click(function() {
        $('.payment-method').removeClass('active');
        $(this).addClass('active');
        $(this).find('input[type="radio"]').prop('checked', true);
    });

    // 주문자 정보와 동일 체크박스
    $('#same-info').change(function() {
        updateAddressFields();
        if ($(this).is(':checked')) {
            $('#receiverName').val($('#ordererName').val());
            $('#receiverPhone').val($('#ordererPhone').val());
            validateField('receiverName');
            validateField('receiverPhone');
        } else {
            $('#receiverName').val('');
            $('#receiverPhone').val('');
            $('#receiverZip').val('');
            $('#receiverAddr').val('');
            $('#receiverAddrDetail').val('');
            $('#deliveryRequest').val('');
            clearFieldError('receiverName');
            clearFieldError('receiverPhone');
            clearFieldError('receiverAddr');
            validationState.receiverName = false;
            validationState.receiverPhone = false;
            validationState.receiverAddr = false;
        }
        updatePaymentButton();
    });

    // 쿠폰 선택
    $('#couponSelect').change(function() {
        if ($(this).prop('disabled')) {
            return;
        }

        const selectedOption = $(this).find('option:selected');
        applyCoupon(selectedOption);
    });

    // 할인코드 적용
    $('#applyDiscountCode').click(function() {
        if ($(this).prop('disabled') || $(this).hasClass('loading')) {
            return;
        }

        const code = $('#discountCode').val().trim();
        if (code) {
            applyDiscountCode(code);
        } else {
            showDiscountCodeMessage('할인코드를 입력해주세요.', 'error');
        }
    });

    // 할인코드 입력 시 엔터키 처리
    $('#discountCode').keypress(function(e) {
        if (e.which === 13 && !$(this).prop('disabled') && !$('#applyDiscountCode').hasClass('loading')) {
            $('#applyDiscountCode').click();
        }
    });

    // 전체 동의 체크박스
    $('#agree-all').change(function() {
        const isChecked = $(this).is(':checked');
        $('input[id^="agree-"]').not('#agree-all').prop('checked', isChecked);
        validateAgreements();
    });

    // 개별 동의 체크박스
    $('input[id^="agree-"]').not('#agree-all').change(function() {
        const totalCheckboxes = $('input[id^="agree-"]').not('#agree-all').length;
        const checkedCheckboxes = $('input[id^="agree-"]:checked').not('#agree-all').length;
        $('#agree-all').prop('checked', totalCheckboxes === checkedCheckboxes);
        validateAgreements();
    });

    // 실시간 유효성 검사
    $('#ordererName, #receiverName').on('input', function() {
        validateField(this.id);
    });

    $('#ordererEmail').on('input', function() {
        validateField(this.id);
    });

    $('#receiverAddrDetail').on('input', function() {
        validateField('receiverAddr');
    });
}

// 할인코드 적용 (백엔드 검증)
function applyDiscountCode(code) {
    // 기존 검증 로직...
    if (!code || code.trim().length === 0) {
        showUserMessage('할인코드를 입력해주세요.', 'error');
        return;
    }

    if (code.trim().length > 20) {
        showDiscountCodeMessage('할인코드는 20자 이하로 입력해주세요.', 'error');
        return;
    }

    setDiscountButtonLoading(true);

    $.ajax({
        url: '/order/discount/validate',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            discountId: code.trim(),
            orderAmount: originalAmount
        }),
        success: function(response) {
            setDiscountButtonLoading(false);

            if (response.success) {
                const discountInfo = response.data;
                let discountAmount = response.discountAmount;

                // 🆕 최대 할인 금액 제한 적용
                let isDiscountLimited = false;
                if (discountAmount > MAX_DISCOUNT_AMOUNT) {
                    discountAmount = MAX_DISCOUNT_AMOUNT;
                    isDiscountLimited = true;
                }

                activeDiscount = {
                    type: 'code',
                    code: discountInfo.discountId,
                    discountType: discountInfo.discountType,
                    value: discountInfo.discountValue,
                    amount: discountAmount, // 제한된 할인 금액 사용
                    name: discountInfo.discountDescription || discountInfo.discountId,
                    serverData: discountInfo,
                    isLimited: isDiscountLimited
                };

                disableCouponSection();
                updatePriceDisplay();
                showAppliedDiscount();

                // 할인 메시지 생성
                const discountTypeText = discountInfo.discountType === 'per'
                    ? `${discountInfo.discountValue}% 할인`
                    : `${discountAmount.toLocaleString()}원 할인`;

                let message = `할인코드가 적용되었습니다. ${discountTypeText}!`;

                // 🆕 할인 금액이 제한된 경우 추가 메시지
                if (isDiscountLimited) {
                    message += ` (최대 할인 금액 ${MAX_DISCOUNT_AMOUNT.toLocaleString()}원이 적용되었습니다)`;
                }

                showDiscountCodeMessage(message, 'success');

                $('#discountCode').prop('readonly', true).addClass('readonly-field');
                $('#applyDiscountCode').prop('disabled', true).addClass('disabled-button').text('적용됨');

                console.log('할인코드 적용 성공:', activeDiscount);
            } else {
                showDiscountCodeMessage(response.message || '유효하지 않은 할인코드입니다.', 'error');
                $('#discountCode').focus();
            }
        },
        error: function(xhr, status, error) {
            setDiscountButtonLoading(false);
            // 기존 에러 처리 로직...
            let errorMessage = '할인코드 확인 중 오류가 발생했습니다.';
            if (xhr.responseJSON && xhr.responseJSON.message) {
                errorMessage = xhr.responseJSON.message;
            }
            showDiscountCodeMessage(errorMessage, 'error');
        }
    });
}

// 할인코드 버튼 로딩 상태 관리
function setDiscountButtonLoading(isLoading) {
    const $button = $('#applyDiscountCode');
    const $input = $('#discountCode');

    if (isLoading) {
        $button.addClass('loading').prop('disabled', true);
        $input.prop('disabled', true);
    } else {
        $button.removeClass('loading').prop('disabled', false);
        $input.prop('disabled', false);
    }
}

// 최대 할인 금액 상수 (쿠폰/할인코드만 적용)
const MAX_DISCOUNT_AMOUNT = 50000; // 5만원

// 쿠폰 적용 (단독 사용)
function applyCoupon(selectedOption) {
    if ($('#couponSelect').prop('disabled')) {
        showUserMessage('사용 가능한 쿠폰이 없습니다.', 'info');
        return;
    }

    if (selectedOption.val() === '' || selectedOption.prop('disabled')) {
        removeActiveDiscount();
        return;
    }

    // 할인코드가 이미 적용된 경우 경고
    if (activeDiscount && activeDiscount.type === 'code') {
        showUserMessage('할인코드가 이미 적용되어 있습니다. 할인코드를 해제하고 쿠폰을 적용하시겠습니까?', 'warning');
        if (confirm('할인코드가 이미 적용되어 있습니다. 할인코드를 해제하고 쿠폰을 적용하시겠습니까?')) {
            removeActiveDiscount();
        } else {
            $('#couponSelect').val('');
            return;
        }
    }

    const couponCode = selectedOption.val();
    const couponId = selectedOption.data('coupon-id');
    const discountType = selectedOption.data('type');
    const discountValue = selectedOption.data('value');
    const minAmount = selectedOption.data('min-amount');

    // 최소 주문금액 확인
    if (originalAmount < minAmount) {
        showCouponMessage(`이 쿠폰은 ${minAmount.toLocaleString()}원 이상 주문 시 사용 가능합니다.`, 'error');
        $('#couponSelect').val('');
        return;
    }

    // 할인 금액 계산
    let discountAmount = 0;
    if (discountType === 'per') {
        discountAmount = Math.floor(originalAmount * discountValue / 100);
    } else if (discountType === 'amount') {
        discountAmount = discountValue;
    } else {
        discountAmount = discountValue;
        console.warn('알 수 없는 할인 타입:', discountType, '- 고정 금액으로 처리합니다.');
    }

    // 🆕 최대 할인 금액 제한 적용
    let isDiscountLimited = false;
    if (discountAmount > MAX_DISCOUNT_AMOUNT) {
        discountAmount = MAX_DISCOUNT_AMOUNT;
        isDiscountLimited = true;
    }

    // 쿠폰 적용
    activeDiscount = {
        type: 'coupon',
        code: couponCode,
        couponId: couponId,
        discountType: discountType,
        value: discountValue,
        amount: discountAmount,
        name: selectedOption.text(),
        isLimited: isDiscountLimited
    };

    // 할인코드 섹션 비활성화
    disableDiscountCodeSection();

    updatePriceDisplay();
    updateCouponInfo(selectedOption.text());

    // 할인 메시지 생성
    const discountTypeText = discountType === 'per' ? `${discountValue}% 할인` : `${discountAmount.toLocaleString()}원 할인`;
    let message = `쿠폰이 적용되었습니다. ${discountTypeText}!`;

    // 🆕 할인 금액이 제한된 경우 추가 메시지
    if (isDiscountLimited) {
        message += ` (최대 할인 금액 ${MAX_DISCOUNT_AMOUNT.toLocaleString()}원이 적용되었습니다)`;
    }

    showCouponMessage(message, 'success');
}

// 활성화된 할인 제거
function removeActiveDiscount() {
    if (!activeDiscount) return;

    const wasCode = activeDiscount.type === 'code';

    activeDiscount = null;
    updatePriceDisplay();

    if (wasCode) {
        // 할인코드 제거 시
        hideAppliedDiscount();
        enableCouponSection();
        enableDiscountCodeSection();  // 🆕 할인코드 제거 시에도 할인코드 섹션 복원
        showUserMessage('할인코드가 제거되었습니다.', 'info');
    } else {
        // 쿠폰 제거 시
        $('#couponSelect').val('');
        $('#couponInfo').removeClass('active').html('<i class="fas fa-info-circle"></i><span>사용 가능한 쿠폰을 선택하면 할인이 적용됩니다</span>');
        hideAppliedDiscount();
        enableCouponSection();  // 🆕 쿠폰 제거 시에도 쿠폰 섹션 활성화
        enableDiscountCodeSection();
        showUserMessage('쿠폰이 제거되었습니다.', 'info');
    }

    console.log('할인이 제거되었습니다. 가격이 원래대로 복원됩니다.');
}

// 가격 표시 업데이트 (단일 할인만 표시)
function updatePriceDisplay() {
    if (activeDiscount) {
        $('#discountRow').show();
        $('#discountLabel').text(activeDiscount.type === 'coupon' ? '쿠폰 할인' : '할인코드');
        $('#discountAmount').text(`-₩${activeDiscount.amount.toLocaleString()}`);
    } else {
        $('#discountRow').hide();
    }

    // 최종 금액 계산 (적립금 포함)
    const originalFinalAmount = paymentInfo.finalAmount || 0;
    const totalDiscount = activeDiscount ? activeDiscount.amount : 0;
    currentTotalAmount = Math.max(0, originalFinalAmount - totalDiscount - currentUsePoints);

    // 최종 금액 표시 업데이트
    $('#finalAmount').text(`₩${currentTotalAmount.toLocaleString()}`);
    $('#paymentButtonText').text(`₩${currentTotalAmount.toLocaleString()} 결제하기`);

    // 적립금 관련 업데이트
    updateRewardAmount();
}

// 쿠폰 정보 업데이트
function updateCouponInfo(couponName) {
    $('#couponInfo').addClass('active').html(`<i class="fas fa-check-circle"></i><span>${couponName}이 적용되었습니다</span>`);
}

// 적용된 할인 표시
function showAppliedDiscount() {
    const discountText = `${activeDiscount.name} (${activeDiscount.amount.toLocaleString()}원 할인)`;
    $('#discountText').text(discountText);
    $('#appliedDiscount').show();
}

// 적용된 할인 숨김
function hideAppliedDiscount() {
    $('#appliedDiscount').hide();
}

// 쿠폰 섹션 비활성화
function disableCouponSection() {
    $('#couponSelect').prop('disabled', true).addClass('disabled-coupon');
    $('.coupon-info').first().html('<i class="fas fa-info-circle"></i><span style="color: #999;">할인코드가 적용된 상태에서는 쿠폰을 사용할 수 없습니다</span>');
}

// 쿠폰 섹션 활성화
function enableCouponSection() {
    const hasValidCoupons = $('#couponSelect option[value!=""]').length > 0;

    if (hasValidCoupons) {
        $('#couponSelect')
            .prop('disabled', false)
            .removeClass('disabled-coupon')
            .css('background-color', '')  // 배경색 초기화
            .css('cursor', '');  // 커서 초기화

        $('.coupon-info').first().html('<i class="fas fa-info-circle"></i><span>사용 가능한 쿠폰을 선택하면 할인이 적용됩니다</span>');
    }
}

// 할인코드 섹션 비활성화
function disableDiscountCodeSection() {
    $('#discountCode').prop('disabled', true).addClass('disabled-input');
    $('#applyDiscountCode').prop('disabled', true).addClass('disabled-button');
    $('.coupon-info').last().html('<i class="fas fa-gift"></i><span style="color: #999;">쿠폰이 적용된 상태에서는 할인코드를 사용할 수 없습니다</span>');
}

// 기존 함수를 이 코드로 교체하세요
function enableDiscountCodeSection() {
    $('#discountCode')
        .prop('disabled', false)
        .prop('readonly', false)
        .removeClass('disabled-input')
        .removeClass('readonly-field')
        .val('')  // 입력값 초기화
        .css('background-color', '')  // 배경색 초기화
        .css('cursor', '');  // 커서 초기화

    $('#applyDiscountCode')
        .prop('disabled', false)
        .removeClass('disabled-button')
        .removeClass('loading')
        .html('<span class="btn-text">적용</span><span class="btn-loading"><i class="fas fa-spinner fa-spin"></i> 확인중</span>')
        .css('background', '')  // 배경색 초기화
        .css('cursor', '');  // 커서 초기화

    $('.coupon-info').last().html('<i class="fas fa-gift"></i><span>할인코드는 대소문자를 구분합니다</span>');
}

// 할인 제거 함수 (전역 함수로 노출)
function removeDiscount() {
    removeActiveDiscount();
}

// 주소 필드 표시/숨김 관리
function updateAddressFields() {
    const isSameInfo = $('#same-info').is(':checked');
    const addressFields = $('#addressFields');

    if (isSameInfo) {
        // 회원 정보가 있는 경우 주소 필드 자동 채움
        /*[# th:if="${member != null}" #]*/
        $('#receiverZip').val(/*[[${member?.memberZip}]]*/ '');
        $('#receiverAddr').val(/*[[${member?.memberAddr}]]*/ '');
        /*[/]*/

        // 주소 필드 숨김
        addressFields.addClass('hidden');

        // 주소 관련 유효성 검사 무시
        validationState.receiverAddr = true;
    } else {
        // 주소 필드 표시
        addressFields.removeClass('hidden');

        // 주소 유효성 검사 다시 적용
        validateField('receiverAddr');
    }

    updatePaymentButton();
}

function validateField(fieldId) {
    const field = $('#' + fieldId);
    const value = field.val().trim();
    let isValid = false;
    let errorMessage = '';

    switch (fieldId) {
        case 'ordererName':
        case 'receiverName':
            isValid = value.length >= 2;
            errorMessage = isValid ? '' : '이름을 2글자 이상 입력해주세요.';
            break;

        case 'ordererPhone':
        case 'receiverPhone':
            isValid = validatePhone(value);
            errorMessage = isValid ? '' : '올바른 휴대폰 번호를 입력해주세요. (예: 010-1234-5678)';
            break;

        case 'ordererEmail':
            isValid = validateEmail(value);
            errorMessage = isValid ? '' : '올바른 이메일 형식을 입력해주세요.';
            break;

        case 'receiverAddr':
            if ($('#same-info').is(':checked')) {
                isValid = true;
            } else {
                const zip = $('#receiverZip').val().trim();
                const addr = $('#receiverAddr').val().trim();
                const addrDetail = $('#receiverAddrDetail').val().trim();
                isValid = zip.length > 0 && addr.length > 0 && addrDetail.length > 0;
                errorMessage = isValid ? '' : '주소를 모두 입력해주세요.';
            }
            break;
    }

    validationState[fieldId] = isValid;

    if (isValid) {
        clearFieldError(fieldId);
    } else if (value.length > 0) {
        showFieldError(fieldId, errorMessage);
    } else {
        clearFieldError(fieldId);
    }

    updatePaymentButton();
    return isValid;
}

function validateAgreements() {
    validationState.agreeTerms = $('#agree-terms').is(':checked');
    validationState.agreePrivacy = $('#agree-privacy').is(':checked');
    validationState.agreeAdult = $('#agree-adult').is(':checked');
    updatePaymentButton();
}

function validateAllFields() {
    validateField('ordererName');
    validateField('ordererPhone');
    validateField('ordererEmail');
    validateField('receiverName');
    validateField('receiverPhone');
    validateField('receiverAddr');
    validateAgreements();
}

function showFieldError(fieldId, message) {
    const field = $('#' + fieldId);
    const errorElement = $('#' + fieldId + '-error');

    field.addClass('error');
    errorElement.text(message).show();
}

function clearFieldError(fieldId) {
    const field = $('#' + fieldId);
    const errorElement = $('#' + fieldId + '-error');

    field.removeClass('error');
    errorElement.hide();
}

function updatePaymentButton() {
    const isValid = isFormValid();
    $('#paymentButton').prop('disabled', !isValid);

    if (isValid) {
        $('#paymentButton').removeClass('disabled');
    } else {
        $('#paymentButton').addClass('disabled');
    }
}

function isFormValid() {
    return Object.values(validationState).every(isValid => isValid);
}

function processPayment() {
    if (!isFormValid()) {
        showUserMessage('모든 필수 항목을 올바르게 입력해주세요.', 'error');
        return;
    }
}

function showUserMessage(message, type) {
    $('.user-message').remove();

    const messageClass = type === 'error' ? 'error-message' : (type === 'success' ? 'success-message' : 'info-message');
    const iconClass = type === 'error' ? 'fas fa-exclamation-circle' : (type === 'success' ? 'fas fa-check-circle' : 'fas fa-info-circle');
    const bgColor = type === 'error' ? '#fff2f2' : (type === 'success' ? '#f0f9ff' : '#f0f9ff');
    const borderColor = type === 'error' ? '#ffcccc' : (type === 'success' ? '#b3e5fc' : '#b3e5fc');
    const textColor = type === 'error' ? '#d63060' : (type === 'success' ? '#0277bd' : '#0277bd');

    const messageHtml = `
            <div class="user-message ${messageClass}" style="
                display: flex;
                align-items: center;
                gap: 8px;
                background: ${bgColor};
                border: 1px solid ${borderColor};
                color: ${textColor};
                padding: 16px;
                border-radius: 12px;
                margin-bottom: 20px;
                font-size: 14px;
                font-weight: 500;
                box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                animation: slideDown 0.3s ease-out;
            ">
                <i class="${iconClass}"></i>
                <span>${message}</span>
                <button onclick="$(this).closest('.user-message').fadeOut()" style="
                    margin-left: auto;
                    background: none;
                    border: none;
                    color: ${textColor};
                    cursor: pointer;
                    font-size: 16px;
                    padding: 0;
                    width: 20px;
                    height: 20px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                ">×</button>
            </div>
        `;

    $('.page-title').after(messageHtml);

    setTimeout(() => {
        $('.user-message').fadeOut();
    }, 5000);
}

function execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            let addr = '';
            let extraAddr = '';

            if (data.userSelectedType === 'R') {
                addr = data.roadAddress;
            } else {
                addr = data.jibunAddress;
            }

            if(data.userSelectedType === 'R'){
                if(data.bname !== '' && /[동|로|가]$/g.test(data.bname)){
                    extraAddr += data.bname;
                }
                if(data.buildingName !== '' && data.apartment === 'Y'){
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                if(extraAddr !== ''){
                    extraAddr = ' (' + extraAddr + ')';
                }
                addr += extraAddr;
            }

            $('#receiverZip').val(data.zonecode);
            $('#receiverAddr').val(addr);
            $('#receiverAddrDetail').focus();

            validateField('receiverAddr');
        }
    }).open();
}

function validateEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function validatePhone(phone) {
    const cleanPhone = phone.replace(/[^0-9]/g, '');
    const re = /^01[0-9]{8,9}$/;
    return re.test(cleanPhone);
}

let isPaymentProcessing = false;

window.addEventListener('beforeunload', function(e) {
    if (isPaymentProcessing) {
        e.preventDefault();
        e.returnValue = '결제가 진행 중입니다. 페이지를 떠나시겠습니까?';
    }
});

// 글로벌 함수 노출
window.forceResetPaymentButton = forceResetPaymentButton;
window.removeDiscount = removeDiscount;

function forceResetPaymentButton() {
    const button = document.getElementById('paymentButton');  // 결제 버튼
    button.disabled = false;  // 버튼을 활성화
    button.classList.remove('processing');  // 'processing' 클래스 제거
    button.innerHTML = `<i class="fas fa-lock" style="margin-right: 8px;"></i><span id="paymentButtonText">₩${currentTotalAmount.toLocaleString()} 결제하기</span>`;  // 버튼 텍스트 복원
}

// 테스트 함수들 (개발자 도구에서 사용 가능)
window.testResetButton = function() {
    console.log('테스트: 버튼 강제 복원 실행');
    if (typeof window.forceResetPaymentButton === 'function') {
        window.forceResetPaymentButton();
        console.log('테스트 완료: 버튼 상태를 확인해보세요');
    } else {
        console.error('forceResetPaymentButton 함수를 찾을 수 없습니다');
    }
};

window.testBackendDiscountCode = function(code = 'SAVE2024') {
    console.log('테스트: 백엔드 할인코드 적용');
    $('#discountCode').val(code);
    $('#applyDiscountCode').click();
    console.log('테스트 완료: 백엔드로 할인코드 검증 요청을 보냈습니다');
};

window.testCouponSelect = function() {
    console.log('테스트: 첫 번째 쿠폰 선택');
    const firstOption = $('#couponSelect option[value!=""]:first');
    if (firstOption.length > 0) {
        $('#couponSelect').val(firstOption.val()).trigger('change');
        console.log('테스트 완료: 첫 번째 쿠폰이 선택되었습니다');
    } else {
        console.log('테스트 실패: 사용 가능한 쿠폰이 없습니다');
    }
};

window.resetAllDiscounts = function() {
    console.log('테스트: 모든 할인 초기화');
    removeActiveDiscount();
    console.log('테스트 완료: 모든 할인이 초기화되었습니다');
};

window.debugPayment = function() {
    console.log('=== 결제 디버그 정보 ===');
    console.log('Payment Info:', paymentInfo);
    console.log('Cart Codes:', cartCodes);
    console.log('Cart Items:', cartItems);
    console.log('Active Discount:', activeDiscount);
    console.log('Original Amount:', originalAmount.toLocaleString());
    console.log('Current Total Amount:', currentTotalAmount.toLocaleString());
    console.log('Current Use Points:', currentUsePoints.toLocaleString());
    console.log('Current Points:', currentPoints.toLocaleString());
    console.log('Validation State:', validationState);
    console.log('Form Valid:', isFormValid());
    console.log('Payment Processing:', isPaymentProcessing);

    console.log('--- 가격 세부 정보 ---');
    console.log('상품 금액:', originalAmount.toLocaleString());
    console.log('기본 할인:', (paymentInfo.discountAmount || 0).toLocaleString());
    console.log('배송비:', (paymentInfo.deliveryFee || 0).toLocaleString());
    console.log('서버 최종 금액:', (paymentInfo.finalAmount || 0).toLocaleString());
    if (activeDiscount) {
        console.log('적용된 할인:', activeDiscount.type, '-', activeDiscount.amount.toLocaleString());
    }
    if (currentUsePoints > 0) {
        console.log('사용 적립금:', currentUsePoints.toLocaleString());
    }
    console.log('최종 결제 금액:', currentTotalAmount.toLocaleString());
    console.log('=====================');
};

// 초기화 확인
$(document).ready(function() {
    console.log('페이지 로드 완료 - 할인코드 백엔드 검증 시스템 활성화됨');
    console.log('사용 가능한 테스트 함수:');
    console.log('- window.testBackendDiscountCode(): 백엔드 할인코드 검증 테스트');
    console.log('- window.testCouponSelect(): 쿠폰 선택 테스트');
    console.log('- window.resetAllDiscounts(): 모든 할인 초기화');
    console.log('- window.debugPayment(): 디버그 정보 출력');
    console.log('백엔드 API 엔드포인트: POST /api/discount/validate');
});

// 할인코드 입력 시 대문자 변환
$('#discountCode').on('input keyup paste', function() {
    this.value = this.value.toUpperCase();
});

// 기존 order.html 파일에서 결제하기 버튼 관련 JavaScript만 수정

// 결제하기 버튼 클릭 이벤트 (기존 코드 대체)
document.getElementById('paymentButton').addEventListener('click', function() {
    const selected = document.querySelector('input[name="payment"]:checked').value;
    if (selected === 'card') {
        // 결제 진행 중 상태로 변경
        this.disabled = true;
        this.classList.add('processing');
        this.innerHTML = '<i class="fas fa-spinner fa-spin" style="margin-right: 8px;"></i>주문 처리 중...';

        try {
            // 주문 데이터 수집
            const orderData = collectOrderData();
            // 입력 검증
            if (!validateOrderData(orderData)) {
                resetPaymentButton();
                return;
            }

            // 주문 생성 요청
            createOrder(orderData);

        } catch (error) {
            console.error('주문 처리 중 오류:', error);
            alert('주문 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
            resetPaymentButton();
        }
    } else if (selected === 'trans') {
        alert('실시간 계좌이체가 선택되었습니다. 계좌이체 안내 메시지...');
    }
});

// 주문 데이터 수집
function collectOrderData() {
    const ordererName = document.getElementById('ordererName').value.trim();
    const ordererPhone = document.getElementById('ordererPhone').value.trim();
    const ordererEmail = document.getElementById('ordererEmail').value.trim();
    const receiverName = document.getElementById('receiverName').value.trim();
    const receiverPhone = document.getElementById('receiverPhone').value.trim();
    const receiverZip = document.getElementById('receiverZip').value.trim();
    const receiverAddr = document.getElementById('receiverAddr').value.trim();
    const receiverAddrDetail = document.getElementById('receiverAddrDetail').value.trim();
    const deliveryRequest = document.getElementById('deliveryRequest').value.trim();
    const paymentMethod = document.querySelector('input[name="payment"]:checked')?.value || 'card';
    const agreeTerms = document.getElementById('agree-terms').checked;
    const agreePrivacy = document.getElementById('agree-privacy').checked;
    const agreeAdult = document.getElementById('agree-adult').checked;
    const couponCode   = document.getElementById('couponSelect')?.value   || '';
    const discountCode = document.getElementById('discountCode')?.value   || '';


    let productCode, optionCode, quantity;

    if (isDirect) {
        productCode = /*[[${productCode}]]*/ null;
        optionCode = /*[[${optionCode}]]*/ null;
        quantity = /*[[${quantity}]]*/ null;
    } else {
        productCode = null;
        optionCode = null;
        quantity = null;
    }

    // 🆕 적립금 정보 추가
    return {
        ordererName, ordererPhone, ordererEmail,
        receiverName, receiverPhone, receiverZip, receiverAddr, receiverAddrDetail, deliveryRequest,
        paymentMethod,
        totalAmount: paymentInfo.totalAmount,
        discountAmount: activeDiscount ? activeDiscount.amount : 0,
        deliveryFee: paymentInfo.deliveryFee,
        finalAmount: 500,
        cartCodes: isDirect ? [] : cartCodes,
        productCode, optionCode, quantity,
        couponCode, discountCode, agreeTerms, agreePrivacy, agreeAdult,
        usePoints: currentUsePoints  // 🆕 적립금 사용 정보 추가
    };
}

// 주문 데이터 검증
function validateOrderData(data) {
    if (!data.ordererName) {
        alert('주문자 이름을 입력해주세요.');
        document.getElementById('ordererName').focus();
        return false;
    }
    if (!data.ordererPhone) {
        alert('주문자 연락처를 입력해주세요.');
        document.getElementById('ordererPhone').focus();
        return false;
    }
    if (!data.ordererEmail) {
        alert('주문자 이메일을 입력해주세요.');
        document.getElementById('ordererEmail').focus();
        return false;
    }
    if (!data.receiverName) {
        alert('받는 분 이름을 입력해주세요.');
        document.getElementById('receiverName').focus();
        return false;
    }
    if (!data.receiverPhone) {
        alert('받는 분 연락처를 입력해주세요.');
        document.getElementById('receiverPhone').focus();
        return false;
    }
    if (!data.receiverZip || !data.receiverAddr) {
        alert('배송지 주소를 입력해주세요.');
        return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(data.ordererEmail)) {
        alert('올바른 이메일 형식을 입력해주세요.');
        document.getElementById('ordererEmail').focus();
        return false;
    }

    const phoneRegex = /^01[0-9]-?[0-9]{4}-?[0-9]{4}$/;
    if (!phoneRegex.test(data.ordererPhone.replace(/-/g, ''))) {
        alert('올바른 연락처를 입력해주세요. (예: 010-1234-5678)');
        document.getElementById('ordererPhone').focus();
        return false;
    }

    if (!phoneRegex.test(data.receiverPhone.replace(/-/g, ''))) {
        alert('올바른 받는 분 연락처를 입력해주세요.');
        document.getElementById('receiverPhone').focus();
        return false;
    }

    if (!data.agreeTerms) {
        alert('이용약관에 동의해주세요.');
        return false;
    }

    if (!data.agreePrivacy) {
        alert('개인정보 처리방침에 동의해주세요.');
        return false;
    }

    if (!data.agreeAdult) {
        alert('성인용품 구매 동의 및 성인인증 확인을 해주세요.');
        return false;
    }

    if (data.finalAmount <= 0) {
        alert('주문 금액이 올바르지 않습니다.');
        return false;
    }

    return true;
}

// 주문 생성 요청
function createOrder(orderData) {
    const endpoint = isDirect ? '/order/create-direct-order' : '/order/create-order';

    console.log('주문 생성 요청:', orderData);

    fetch(endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(orderData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('주문 생성 응답:', data);

            if (data.success) {
                document.getElementById("goodsNm").value = data.goodsNm;
                document.getElementById("ordNo").value = data.orderId;
                document.getElementById("ordNm").value = data.ordNm;
                document.getElementById("ordTel").value = data.ordTel;
                document.getElementById("ordEmail").value = data.ordEmail;
                document.getElementById("goodsAmt").value = data.goodsAmt;
                document.getElementById("ediDate").value = data.ediDate;
                document.getElementById("encData").value = data.encData;
                document.getElementById("userIp").value = data.userIp;

                document.getElementById('payBtn').click();
                // window.location.href = `/pay`;
                //window.location.href = `/order/order-complete/${data.orderId}`;
                // 결제 기능 추가 시 여기서 PG사 결제 처리
                // processPayment(data.orderCode, orderData.finalAmount);
            } else {
                alert('주문 처리 실패: ' + (data.message || '알 수 없는 오류가 발생했습니다.'));
                resetPaymentButton();
            }
        })
        .catch(error => {
            console.error('주문 요청 오류:', error);
            alert('주문 처리 중 네트워크 오류가 발생했습니다. 인터넷 연결을 확인하고 다시 시도해주세요.');
            resetPaymentButton();
        });
}

// 결제 완료 후 PG SDK가 호출
function pay_result_submit(){
    payResultSubmit();  // 위 form을 returnUrl로 POST
}
function pay_result_close(){
    // 팝업 닫힘 처리 (선택)
}

// 결제 버튼 상태 초기화
function resetPaymentButton() {
    const button = document.getElementById('paymentButton');
    button.disabled = false;
    button.classList.remove('processing');
    button.innerHTML = `<i class="fas fa-lock" style="margin-right: 8px;"></i><span id="paymentButtonText">₩${currentTotalAmount.toLocaleString()} 결제하기</span>`;
}

// 🆕 적립금 관련 글로벌 함수 노출
window.getCurrentUsePoints = function() {
    return currentUsePoints;
};

window.addPointsToPaymentForm = function() {
    // 기존 결제 폼에 적립금 사용 정보 추가
    if ($('input[name="usePoints"]').length === 0) {
        $('<input>').attr({
            type: 'hidden',
            name: 'usePoints',
            value: currentUsePoints
        }).appendTo('#paymentForm, form');
    } else {
        $('input[name="usePoints"]').val(currentUsePoints);
    }
};

function showDiscountCodeMessage(message, type) {
    const messageDiv = $('#discountCodeMessage');
    const messageText = messageDiv.find('.message-text');

    messageText.text(message);
    messageDiv.removeClass('error success warning info').addClass(type + ' show');

    setTimeout(() => hideDiscountCodeMessage(), 3000);
}

function hideDiscountCodeMessage() {
    $('#discountCodeMessage').removeClass('show');
}

function showCouponMessage(message, type) {
    const messageDiv = $('#couponMessage');
    const messageText = messageDiv.find('.message-text');

    messageText.text(message);
    messageDiv.removeClass('error success warning info').addClass(type + ' show');

    setTimeout(() => hideCouponMessage(), 3000);
}

function hideCouponMessage() {
    $('#couponMessage').removeClass('show');
}