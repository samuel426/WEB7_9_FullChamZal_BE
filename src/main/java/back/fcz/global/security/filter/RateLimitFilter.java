package back.fcz.global.security.filter;

import back.fcz.domain.sanction.service.RateLimitService;
import back.fcz.global.security.jwt.JwtProvider;
import back.fcz.global.security.jwt.util.CookieUtil;
import back.fcz.global.util.RequestInfoExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Rate Limit 검증 필터
 * 쿨다운 상태의 사용자 요청을 제한합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 쿠키에서 Access Token 추출
        Optional<String> tokenOptional = CookieUtil.getCookieValue(request, CookieUtil.ACCESS_TOKEN_COOKIE);
        Long memberId = null;

        if (tokenOptional.isPresent()) {
            try {
                String token = tokenOptional.get();
                memberId = jwtProvider.extractMemberId(token);
            } catch (Exception e) {
                log.debug("토큰 파싱 실패 (Rate Limit 필터에서는 무시): {}", e.getMessage());
            }
        }

        // 회원 쿨다운 확인
        if (memberId != null && rateLimitService.isInCooldown(memberId)) {
            long remainingSeconds = rateLimitService.getRemainingCooldown(memberId);
            log.warn("쿨다운 중인 회원의 접근 시도: {} (남은 시간: {}초)", memberId, remainingSeconds);
            sendRateLimitResponse(response, remainingSeconds);
            return;
        }

        // IP 쿨다운 확인
        String clientIp = RequestInfoExtractor.extractIp(request);
        if (rateLimitService.isInCooldownByIp(clientIp)) {
            long remainingSeconds = rateLimitService.getRemainingCooldownByIp(clientIp);
            log.warn("쿨다운 중인 IP의 접근 시도: {} (남은 시간: {}초)", clientIp, remainingSeconds);
            sendRateLimitResponse(response, remainingSeconds);
            return;
        }

        // 정상 요청은 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    // Rate Limit 초과 응답 전송
    private void sendRateLimitResponse(HttpServletResponse response, long remainingSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429 반환
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(remainingSeconds));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", "RATE_LIMIT_EXCEEDED");
        errorResponse.put("message", "요청 횟수 제한을 초과했습니다.");
        errorResponse.put("detail", "잠시 후 다시 시도해주세요.");
        errorResponse.put("retryAfterSeconds", remainingSeconds);

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}