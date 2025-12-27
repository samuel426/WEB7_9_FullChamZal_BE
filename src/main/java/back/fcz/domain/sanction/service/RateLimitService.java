package back.fcz.domain.sanction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

// Redis 기반 Rate Limiting 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis 키 접두사
    private static final String RATE_LIMIT_PREFIX_MEMBER = "ratelimit:member:";
    private static final String RATE_LIMIT_PREFIX_IP = "ratelimit:ip:";

    // 일반 사용자 기본 제한: 1분에 20회
    private static final int DEFAULT_MAX_REQUESTS = 20;
    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1);

    // 의심 사용자 엄격한 제한: 10분에 5회
    private static final int SUSPICIOUS_MAX_REQUESTS = 5;
    private static final Duration SUSPICIOUS_WINDOW = Duration.ofMinutes(10);

    // 고위험 사용자 매우 엄격한 제한: 10분에 3회
    private static final int HIGH_RISK_MAX_REQUESTS = 3;
    private static final Duration HIGH_RISK_WINDOW = Duration.ofMinutes(10);

    // 회원의 요청이 Rate Limit을 초과했는지 확인
    public boolean isRateLimitExceeded(Long memberId, int riskLevel) {
        String key = RATE_LIMIT_PREFIX_MEMBER + memberId;

        int maxRequests;
        Duration window;

        // 위험 수준에 따라 제한 설정
        switch (riskLevel) {
            case 2 -> {
                maxRequests = HIGH_RISK_MAX_REQUESTS;
                window = HIGH_RISK_WINDOW;
            }
            case 1 -> {
                maxRequests = SUSPICIOUS_MAX_REQUESTS;
                window = SUSPICIOUS_WINDOW;
            }
            default -> {
                maxRequests = DEFAULT_MAX_REQUESTS;
                window = DEFAULT_WINDOW;
            }
        }

        return isLimitExceeded(key, maxRequests, window, "회원 " + memberId);
    }

    // 비회원(= IP)의 요청이 Rate Limit을 초과했는지 확인
    public boolean isRateLimitExceededByIp(String ipAddress, int riskLevel) {
        String key = RATE_LIMIT_PREFIX_IP + ipAddress;

        int maxRequests;
        Duration window;

        // 위험 수준에 따라 제한 설정
        switch (riskLevel) {
            case 2 -> {
                maxRequests = HIGH_RISK_MAX_REQUESTS;
                window = HIGH_RISK_WINDOW;
            }
            case 1 -> {
                maxRequests = SUSPICIOUS_MAX_REQUESTS;
                window = SUSPICIOUS_WINDOW;
            }
            default -> {
                maxRequests = DEFAULT_MAX_REQUESTS;
                window = DEFAULT_WINDOW;
            }
        }

        return isLimitExceeded(key, maxRequests, window, "IP " + ipAddress);
    }

    // 회원의 Rate Limit 쿨다운 적용 -> 의심 활동 감지 시 일정 시간 동안 요청 제한
    public void applyCooldown(Long memberId, int cooldownMinutes) {
        String key = RATE_LIMIT_PREFIX_MEMBER + memberId + ":cooldown";

        try {
            redisTemplate.opsForValue().set(
                    key,
                    String.valueOf(System.currentTimeMillis()),
                    Duration.ofMinutes(cooldownMinutes)
            );
            log.warn("쿨다운 적용: 회원 {} → {}분간 요청 제한", memberId, cooldownMinutes);
        } catch (Exception e) {
            log.error("쿨다운 적용 실패: 회원 {}", memberId, e);
        }
    }

    // 비회원(= IP)의 Rate Limit 쿨다운 적용
    public void applyCooldownByIp(String ipAddress, int cooldownMinutes) {
        String key = RATE_LIMIT_PREFIX_IP + ipAddress + ":cooldown";

        try {
            redisTemplate.opsForValue().set(
                    key,
                    String.valueOf(System.currentTimeMillis()),
                    Duration.ofMinutes(cooldownMinutes)
            );
            log.warn("쿨다운 적용: IP {} → {}분간 요청 제한", ipAddress, cooldownMinutes);
        } catch (Exception e) {
            log.error("쿨다운 적용 실패: IP {}", ipAddress, e);
        }
    }

    // 회원이 쿨타임 중인지 확인
    public boolean isInCooldown(Long memberId) {
        String key = RATE_LIMIT_PREFIX_MEMBER + memberId + ":cooldown";

        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("쿨다운 중: 회원 {}", memberId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("쿨다운 확인 실패: 회원 {}", memberId, e);
            return false;
        }
    }

    // 비회원(= IP)이 쿨타임 중인지 확인
    public boolean isInCooldownByIp(String ipAddress) {
        String key = RATE_LIMIT_PREFIX_IP + ipAddress + ":cooldown";

        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("쿨다운 중: IP {}", ipAddress);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("쿨다운 확인 실패: IP {}", ipAddress, e);
            return false;
        }
    }

    // 회원의 남은 쿨타임 시간 조회 (초 단위)
    public long getRemainingCooldown(Long memberId) {
        String key = RATE_LIMIT_PREFIX_MEMBER + memberId + ":cooldown";

        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("쿨다운 시간 조회 실패: 회원 {}", memberId, e);
            return 0;
        }
    }

    // 비회원(= IP)의 남은 쿨타임 시간 조회 (초 단위)
    public long getRemainingCooldownByIp(String ipAddress) {
        String key = RATE_LIMIT_PREFIX_IP + ipAddress + ":cooldown";

        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("쿨다운 시간 조회 실패: IP {}", ipAddress, e);
            return 0;
        }
    }

    // Rate Limit 초과 여부 확인
    private boolean isLimitExceeded(String key, int maxRequests, Duration window, String identifier) {
        try {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - window.toMillis();

            redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

            Long currentCount = redisTemplate.opsForZSet().zCard(key);

            if (currentCount != null && currentCount >= maxRequests) {
                log.warn("Rate Limit 초과: {} (현재: {}, 최대: {})",
                        identifier, currentCount, maxRequests);
                return true;
            }

            redisTemplate.opsForZSet().add(key, String.valueOf(currentTime), currentTime);

            redisTemplate.expire(key, window.multipliedBy(2));

            log.debug("Rate Limit 체크: {} (현재: {}/{}, 윈도우: {}분)",
                    identifier, currentCount != null ? currentCount + 1 : 1,
                    maxRequests, window.toMinutes());

            return false;

        } catch (Exception e) {
            log.error("Rate Limit 확인 실패: {}", identifier, e);
            // Redis 장애 시 제한하지 않음 (정상 접근 허용)
            return false;
        }
    }

    // 회원의 Rate Limit 카운터 초기화
    public void resetRateLimit(Long memberId) {
        String key = RATE_LIMIT_PREFIX_MEMBER + memberId;
        String cooldownKey = key + ":cooldown";

        try {
            redisTemplate.delete(key);
            redisTemplate.delete(cooldownKey);
            log.info("Rate Limit 초기화 완료: 회원 {}", memberId);
        } catch (Exception e) {
            log.error("Rate Limit 초기화 실패: 회원 {}", memberId, e);
        }
    }

    // 비회원(= IP)의 Rate Limit 카운터 초기화
    public void resetRateLimitByIp(String ipAddress) {
        String key = RATE_LIMIT_PREFIX_IP + ipAddress;
        String cooldownKey = key + ":cooldown";

        try {
            redisTemplate.delete(key);
            redisTemplate.delete(cooldownKey);
            log.info("Rate Limit 초기화 완료: IP {}", ipAddress);
        } catch (Exception e) {
            log.error("Rate Limit 초기화 실패: IP {}", ipAddress, e);
        }
    }
}
