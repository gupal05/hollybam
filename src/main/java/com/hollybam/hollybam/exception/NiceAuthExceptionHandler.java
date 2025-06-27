package com.hollybam.hollybam.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * NICE 본인인증 관련 예외 처리
 * 운영환경에서 안전한 오류 처리를 위한 글로벌 예외 핸들러
 */
@Slf4j
@ControllerAdvice
public class NiceAuthExceptionHandler {

    /**
     * 클라이언트 연결 중단 예외 처리 (정상적인 상황)
     */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException ex) {
        // 클라이언트가 연결을 끊은 경우 - 로그 없이 무시 (정상적인 상황)
        log.debug("클라이언트 연결 종료: {}", ex.getMessage());
    }

    /**
     * NICE API 토큰 관련 예외 처리
     */
    @ExceptionHandler(NiceTokenException.class)
    public ResponseEntity<Map<String, Object>> handleNiceTokenException(
            NiceTokenException ex, WebRequest request) {

        log.error("NICE 토큰 오류 발생: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                "NICE_TOKEN_ERROR",
                "인증 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.SERVICE_UNAVAILABLE
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * NICE 암호화 관련 예외 처리
     */
    @ExceptionHandler(NiceCryptoException.class)
    public ResponseEntity<Map<String, Object>> handleNiceCryptoException(
            NiceCryptoException ex, WebRequest request) {

        log.error("NICE 암호화 오류 발생: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                "NICE_CRYPTO_ERROR",
                "보안 처리 중 오류가 발생했습니다. 다시 시도해주세요.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 미성년자 접근 예외 처리
     */
    @ExceptionHandler(MinorAccessException.class)
    public ResponseEntity<Map<String, Object>> handleMinorAccessException(
            MinorAccessException ex, WebRequest request) {

        log.warn("미성년자 접근 차단: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "MINOR_ACCESS_DENIED",
                "만 19세 미만은 이용할 수 없습니다.",
                HttpStatus.FORBIDDEN
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * NICE 인증 실패 예외 처리
     */
    @ExceptionHandler(NiceAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleNiceAuthenticationException(
            NiceAuthenticationException ex, WebRequest request) {

        log.error("NICE 인증 실패: {}", ex.getMessage());

        String userMessage = getUserFriendlyMessage(ex.getErrorCode(), ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "NICE_AUTH_FAILED",
                userMessage,
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 세션 만료 예외 처리
     */
    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleSessionExpiredException(
            SessionExpiredException ex, WebRequest request) {

        log.warn("세션 만료: {}", ex.getMessage());

        Map<String, Object> errorResponse = createErrorResponse(
                "SESSION_EXPIRED",
                "세션이 만료되었습니다. 다시 시도해주세요.",
                HttpStatus.UNAUTHORIZED
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 일반적인 런타임 예외 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(
            RuntimeException ex, WebRequest request, HttpServletRequest httpRequest) {

        log.error("예상치 못한 런타임 오류: {}", ex.getMessage(), ex);

        // 정적 리소스 요청인지 확인
        if (isStaticResourceRequest(httpRequest)) {
            log.debug("정적 리소스 요청으로 판단됨: {}", httpRequest.getRequestURI());
            return ResponseEntity.notFound().build();
        }

        // AJAX 요청이면 JSON으로 응답
        if (isAjaxRequest(httpRequest)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "INTERNAL_ERROR",
                    "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        // 일반 웹 요청
        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_ERROR",
                "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 모든 예외에 대한 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(
            Exception ex, WebRequest request, HttpServletRequest httpRequest) {

        log.error("처리되지 않은 예외 발생: {}", ex.getClass().getSimpleName(), ex);

        // 정적 파일 요청인지 확인
        if (isStaticResourceRequest(httpRequest)) {
            log.debug("정적 리소스 요청으로 판단됨: {}", httpRequest.getRequestURI());
            return ResponseEntity.notFound().build();
        }

        // AJAX 요청이면 JSON으로 응답
        if (isAjaxRequest(httpRequest)) {
            Map<String, Object> errorResponse = createErrorResponse(
                    "UNKNOWN_ERROR",
                    "알 수 없는 오류가 발생했습니다. 고객센터에 문의해주세요.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }

        // 일반 웹 요청이면 에러 페이지로 리다이렉트 시도
        try {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/error"))
                    .build();
        } catch (Exception redirectEx) {
            // 리다이렉트도 실패하면 JSON으로 응답
            Map<String, Object> errorResponse = createErrorResponse(
                    "UNKNOWN_ERROR",
                    "알 수 없는 오류가 발생했습니다. 고객센터에 문의해주세요.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    /**
     * 정적 리소스 요청 판별
     */
    private boolean isStaticResourceRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String userAgent = request.getHeader("User-Agent");

        // 정적 파일 확장자 체크
        if (requestURI != null) {
            String lowerURI = requestURI.toLowerCase();
            if (lowerURI.endsWith(".ico") || lowerURI.endsWith(".png") ||
                    lowerURI.endsWith(".jpg") || lowerURI.endsWith(".jpeg") ||
                    lowerURI.endsWith(".gif") || lowerURI.endsWith(".css") ||
                    lowerURI.endsWith(".js") || lowerURI.endsWith(".webp") ||
                    lowerURI.endsWith(".svg") || lowerURI.endsWith(".woff") ||
                    lowerURI.endsWith(".woff2") || lowerURI.endsWith(".ttf") ||
                    lowerURI.contains("/favicon") || lowerURI.contains("/static/") ||
                    lowerURI.contains("/css/") || lowerURI.contains("/js/") ||
                    lowerURI.contains("/images/") || lowerURI.contains("/fonts/")) {
                return true;
            }
        }

        // Accept 헤더로 정적 리소스 요청 판단
        if (accept != null) {
            String lowerAccept = accept.toLowerCase();
            if (lowerAccept.contains("image/") ||
                    lowerAccept.contains("text/css") ||
                    lowerAccept.contains("application/javascript") ||
                    lowerAccept.contains("font/") ||
                    lowerAccept.contains("application/font")) {
                return true;
            }
        }

        // 브라우저가 자동으로 보내는 요청들 (favicon 등)
        if (userAgent != null && requestURI != null &&
                (requestURI.contains("favicon") || requestURI.contains("apple-touch-icon"))) {
            return true;
        }

        return false;
    }

    /**
     * AJAX 요청 판별
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        String contentType = request.getHeader("Content-Type");

        return "XMLHttpRequest".equals(xRequestedWith) ||
                (accept != null && accept.contains("application/json")) ||
                (contentType != null && contentType.contains("application/json"));
    }

    /**
     * 공통 오류 응답 생성
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());

        return errorResponse;
    }

    /**
     * 사용자 친화적 오류 메시지 변환
     */
    private String getUserFriendlyMessage(String errorCode, String originalMessage) {
        if (errorCode == null) {
            return "본인인증 중 오류가 발생했습니다.";
        }

        switch (errorCode) {
            case "0001":
                return "입력하신 정보가 일치하지 않습니다. 통신사, 이름, 생년월일, 휴대폰번호를 다시 확인해주세요.";
            case "0003":
                return "인증 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            case "0012":
                return "입력 정보에 오류가 있습니다. 다시 확인해주세요.";
            case "0013":
            case "0014":
            case "0015":
            case "0016":
            case "0017":
                return "보안 처리 중 오류가 발생했습니다. 다시 시도해주세요.";
            case "0018":
                return "네트워크 연결에 문제가 있습니다. 인터넷 연결을 확인하고 다시 시도해주세요.";
            case "0019":
                return "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            case "0020":
            case "0021":
            case "0022":
            case "0023":
                return "서비스 이용에 제한이 있습니다. 고객센터에 문의해주세요.";
            case "0031":
                return "유효한 인증 이력이 없습니다. 처음부터 다시 시도해주세요.";
            default:
                return "본인인증 중 오류가 발생했습니다. 고객센터에 문의해주세요.";
        }
    }

    // === 커스텀 예외 클래스들 ===

    /**
     * NICE 토큰 관련 예외
     */
    public static class NiceTokenException extends RuntimeException {
        public NiceTokenException(String message) {
            super(message);
        }

        public NiceTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * NICE 암호화 관련 예외
     */
    public static class NiceCryptoException extends RuntimeException {
        public NiceCryptoException(String message) {
            super(message);
        }

        public NiceCryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * 미성년자 접근 예외
     */
    public static class MinorAccessException extends RuntimeException {
        public MinorAccessException(String message) {
            super(message);
        }
    }

    /**
     * NICE 인증 실패 예외
     */
    public static class NiceAuthenticationException extends RuntimeException {
        private final String errorCode;

        public NiceAuthenticationException(String message) {
            super(message);
            this.errorCode = null;
        }

        public NiceAuthenticationException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }

    /**
     * 세션 만료 예외
     */
    public static class SessionExpiredException extends RuntimeException {
        public SessionExpiredException(String message) {
            super(message);
        }
    }
}