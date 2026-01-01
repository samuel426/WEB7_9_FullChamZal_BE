package back.fcz.global.security.filter;

import back.fcz.domain.member.service.CurrentUserContext;
import back.fcz.domain.sanction.service.MonitoringService;
import back.fcz.domain.sanction.service.RateLimitService;
import back.fcz.domain.sanction.util.RequestInfoExtractor;
import back.fcz.global.exception.ErrorCode;
import back.fcz.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate Limit 검증 필터
 * 쿨다운 상태의 사용자 요청을 제한합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final MonitoringService monitoringService;
    private final CurrentUserContext currentUserContext;

    private static final String[] EXCLUDED_PATHS = {
            "/h2-console",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.startsWith(excludedPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // IP는 항상 추출 (비회원/회원 공통)
        String clientIp = RequestInfoExtractor.extractIp(request);

        // 1) IP 쿨다운 확인
        if (rateLimitService.isInCooldownByIp(clientIp)) {
            long remainingSeconds = rateLimitService.getRemainingCooldownByIp(clientIp);
            log.warn("쿨다운 중인 IP의 접근 시도: {} (남은 시간: {}초)", clientIp, remainingSeconds);
            sendRateLimitResponse(response, remainingSeconds);
            return;
        }

        // 2) 인증된 사용자 여부 확인 (JWT 필터 결과 이용)
        if (currentUserContext.isAuthenticated()) {
            Long memberId = currentUserContext.getCurrentMemberId();

            // 2-1) 회원 쿨다운 확인
            if (rateLimitService.isInCooldown(memberId)) {
                long remainingSeconds = rateLimitService.getRemainingCooldown(memberId);
                log.warn("쿨다운 중인 회원의 접근 시도: {} (남은 시간: {}초)", memberId, remainingSeconds);
                sendRateLimitResponse(response, remainingSeconds);
                return;
            }

            // 2-2) 위험도 산정 (회원 기준)
            int riskLevel = 0;
            int suspicionScore = monitoringService.getSuspicionScore(memberId);
            if (suspicionScore >= 100) {
                riskLevel = 2;
            } else if (suspicionScore >= 50) {
                riskLevel = 1;
            }

            // 2-3) 회원 기준 rate Limit
            if (rateLimitService.isRateLimitExceeded(memberId, riskLevel)) {
                log.warn("Rate Limit 초과: 회원 {}", memberId);
                sendRateLimitResponse(response, 60); // 정책 1분
                return;
            }
        }

        // 3) 비인증 사용자(= IP 기준) 위험도 산정
        else {
            int riskLevel = 0;
            int suspicionScore = monitoringService.getSuspicionScoreByIp(clientIp);
            if (suspicionScore >= 100) {
                riskLevel = 2;
            } else if (suspicionScore >= 50) {
                riskLevel = 1;
            }

            // 3-1) IP 기준 rate Limit
            if (rateLimitService.isRateLimitExceededByIp(clientIp, riskLevel)) {
                log.warn("Rate Limit 초과: 회원 IP {}", clientIp);
                sendRateLimitResponse(response, 60); // 정책 1분
                return;
            }
        }

        // 정상 요청은 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // Rate Limit 초과 응답 전송
    private void sendRateLimitResponse(HttpServletResponse response, long remainingSeconds) throws IOException {
        ErrorCode errorCode = ErrorCode.RATE_LIMIT_COOLDOWN;

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(remainingSeconds));

        // 재시도 시간 포함
        Map<String, Object> data = new HashMap<>();
        data.put("retryAfterSeconds", remainingSeconds);
        data.put("retryAfterMinutes", Math.ceil(remainingSeconds / 60.0));

        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                data
        );

        String json = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(json);
    }
}