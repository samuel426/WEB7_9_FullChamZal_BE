package back.fcz.domain.sanction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

// Redis 기반 의심 점수 모니터링 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SanctionService sanctionService;
    private final IpBlockService ipBlockService;
    private final RateLimitService rateLimitService;

    // Redis 키 접두사
    private static final String SUSPICION_KEY_PREFIX_MEMBER = "suspicion:member:";
    private static final String SUSPICION_KEY_PREFIX_IP = "suspicion:ip:";

    // TTL (30일)
    private static final Duration SUSPICION_TTL = Duration.ofDays(30);

    // 임계값
    private static final int WARNING_THRESHOLD = 30;   // 경고 수준
    private static final int LIMIT_THRESHOLD = 50;     // 제한 수준
    private static final int BLOCK_THRESHOLD = 100;    // 차단 수준

    // 회원의 의심 점수 증가
    public void incrementSuspicionScore(Long memberId, int score) {
        String key = SUSPICION_KEY_PREFIX_MEMBER + memberId;
        incrementScore(key, score, "회원 " + memberId);

        // 임계값 확인 및 제재 적용
        int currentScore = getSuspicionScore(memberId);
        checkAndApplySanction(memberId, null, currentScore);
    }

    //  비회원(IP)의 의심 점수 증가
    public void incrementSuspicionScoreByIp(String ipAddress, int score) {
        String key = SUSPICION_KEY_PREFIX_IP + ipAddress;
        incrementScore(key, score, "IP " + ipAddress);

        // 임계값 확인
        int currentScore = getSuspicionScoreByIp(ipAddress);
        if (currentScore >= BLOCK_THRESHOLD) {
            log.error("IP 차단 필요: {} (점수: {}점)", ipAddress, currentScore);
            ipBlockService.blockIp(
                    ipAddress,
                    "의심 활동 누적 (점수: " + currentScore + "점)"
            );
            resetSuspicionScoreByIp(ipAddress);
        } else if (currentScore >= LIMIT_THRESHOLD) {
            log.warn("IP 제한 필요: {} (점수: {}점)", ipAddress, currentScore);
            rateLimitService.applyCooldownByIp(ipAddress, 30); // 30분 쿨다운
        } else if (currentScore >= WARNING_THRESHOLD) {
            log.info("IP 경고: {} (점수: {}점)", ipAddress, currentScore);
        }
    }

    // 회원의 현재 의심 점수 조회
    public int getSuspicionScore(Long memberId) {
        String key = SUSPICION_KEY_PREFIX_MEMBER + memberId;
        return getScore(key);
    }

    // 비회원(IP)의 현재 의심 점수 조회
    public int getSuspicionScoreByIp(String ipAddress) {
        String key = SUSPICION_KEY_PREFIX_IP + ipAddress;
        return getScore(key);
    }

    // 회원의 의심 점수 초기화
    public void resetSuspicionScore(Long memberId) {
        String key = SUSPICION_KEY_PREFIX_MEMBER + memberId;
        try {
            redisTemplate.delete(key);
            log.info("의심 점수 초기화 완료: 회원 {}", memberId);
        } catch (Exception e) {
            log.error("Redis 점수 초기화 실패: 회원 {}", memberId, e);
        }
    }

    // 비회원(IP)의 의심 점수 초기화
    public void resetSuspicionScoreByIp(String ipAddress) {
        String key = SUSPICION_KEY_PREFIX_IP + ipAddress;
        try {
            redisTemplate.delete(key);
            log.info("의심 점수 초기화 완료: IP {}", ipAddress);
        } catch (Exception e) {
            log.error("Redis 점수 초기화 실패: IP {}", ipAddress, e);
        }
    }

    // 임계값 확인하고 필요 시 제재
    private void checkAndApplySanction(Long memberId, String ipAddress, int currentScore) {
        if (currentScore >= BLOCK_THRESHOLD) {
            // 차단 수준: 계정 정지
            log.error("자동 제재 발동: 회원 {} (점수: {}점) - 계정 정지 7일", memberId, currentScore);
            sanctionService.applyAutoSuspension(
                    memberId,
                    "의심 활동 누적 (점수: " + currentScore + "점)",
                    7
            );
            // 제재 후 점수 초기화
            resetSuspicionScore(memberId);

        } else if (currentScore >= LIMIT_THRESHOLD) {
            // 제한 수준: Rate limiting 적용
            log.warn("Rate Limiting 적용 대상: 회원 {} (점수: {}점)", memberId, currentScore);
            rateLimitService.applyCooldown(memberId, 30); // 30분 쿨다운
        } else if (currentScore >= WARNING_THRESHOLD) {
            // 경고 수준: 로그만 기록
            log.info("경고 수준 도달: 회원 {} (점수: {}점)", memberId, currentScore);
        }
    }

    // 공통 점수 증가 로직
    private void incrementScore(String key, int score, String identifier) {
        try {
            Long newScore = redisTemplate.opsForValue().increment(key, score);
            redisTemplate.expire(key, SUSPICION_TTL);
            log.debug("의심 점수 증가: {} → {}점 (+{}점)", identifier, newScore, score);
        } catch (Exception e) {
            log.error("Redis 점수 증가 실패: {}", identifier, e);
            // Redis 장애 시에도 서비스는 계속 동작하도록 예외를 삼킴
        }
    }

    // 공통 점수 조회 로직
    private int getScore(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            log.error("Redis 점수 조회 실패: {}", key, e);
            return 0; // 조회 실패 시 0 반환
        }
    }
}
