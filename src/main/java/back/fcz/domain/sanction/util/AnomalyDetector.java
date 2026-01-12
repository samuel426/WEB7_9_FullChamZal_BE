package back.fcz.domain.sanction.util;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class AnomalyDetector {

    // 지구 반지름 (km)
    private static final double EARTH_RADIUS_KM = 6371.0;

    // GPS 오차 범위 상수화
    private static final double GPS_ERROR_MARGIN_KM = 0.1;           // 100m: 일반 GPS 오차
    private static final double GPS_RECONNECTION_ERROR_KM = 0.3;     // 300m: GPS 재연결 오차
    private static final double SHORT_MOVEMENT_MARGIN_KM = 0.3;      // 300m: 짧은 시간 이동 허용

    // 3단계 속도 임계값
    private static final double EXTREME_SPEED_KMH = 1000.0;  // 즉시 차단
    private static final double HIGH_SPEED_KMH = 300.0;      // 강한 의심
    private static final double SUSPICIOUS_SPEED_KMH = 150.0; // 누적 의심

    // 시간 동기화 허용 오차 (분)
    private static final int TIME_SYNC_TOLERANCE_MINUTES = 10;

    /**
     * Haversine 공식을 사용하여 두 GPS 좌표 간의 거리 계산
     * @return 두 지점 간의 거리 (km)
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // 위도/경도를 라디안으로 변환
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 두 위치 간 이동 속도를 계산 (시속 km)
     * @return 이동 속도 (km/h), 시간이 0 이하면 0.0 반환
     */
    public static double calculateSpeed(double distance, long timeDiffSeconds) {
        if (timeDiffSeconds <= 0) {
            return -1.0;
        }

        double hours = timeDiffSeconds / 3600.0;
        // 속도(km/h) 반환
        return distance / hours;
    }

    /**
     * 이동 속도를 3단계로 분류하여 의심 수준 반환
     * @return 0: 정상, 1: 누적의심(150+), 2: 강한의심(300+), 3: 즉시차단(1000+)
     */
    public static int classifyMovementAnomaly(
            double previousLat, double previousLng,
            double currentLat, double currentLng,
            LocalDateTime previousTime, LocalDateTime currentTime,
            int duplicateRequestThresholdSeconds) {
        long timeDiffSeconds = Duration.between(previousTime, currentTime).getSeconds();
        double distance = calculateDistance(previousLat, previousLng, currentLat, currentLng);

        log.debug("이동 분석 - 시간차: {}초, 거리: {}km, 이전시각: {}, 현재시각: {}",
                timeDiffSeconds, distance, previousTime, currentTime);

        // === 1단계: 중복 요청 필터링 ===
        if (timeDiffSeconds >= 0 && timeDiffSeconds < duplicateRequestThresholdSeconds) {
            log.debug("중복 요청 감지 ({}초 이내): GPS 오차 무시", timeDiffSeconds);
            return 0;
        }

        // === 2단계: 시간 역행 검증 ===
        if (timeDiffSeconds < 0) {
            if (distance < GPS_ERROR_MARGIN_KM) {
                log.debug("시간 역행이지만 GPS 오차 범위 내: {}초, {}km", timeDiffSeconds, distance);
                return 0;
            }

            log.warn("시간 역행 + 위치 변경 감지: {}초, {}km", timeDiffSeconds, distance);
            return 3;
        }

        // === 3단계: 동일 시간 검증 ===
        if (timeDiffSeconds == 0) {
            // 300m 미만은 GPS 재연결 오차로 간주
            if (distance < GPS_RECONNECTION_ERROR_KM) {
                log.debug("동일 시간이지만 GPS 재연결 오차 범위 내: {}km", distance);
                return 0;
            }

            log.warn("동일 시간 위치 변경 감지: {}km 이동", distance);
            return 3;
        }

        // === 4단계: GPS 오차 범위 검증 ===
        if (distance < GPS_ERROR_MARGIN_KM) {  // 100m
            log.debug("이동 거리 {}km는 GPS 오차 범위 내로 판단", distance);
            return 0;
        }

        // === 5단계: 짧은 시간 + 짧은 거리 추가 관대 처리 ===
        if (timeDiffSeconds < 60 && distance < SHORT_MOVEMENT_MARGIN_KM) {  // 1분 & 300m
            log.debug("짧은 시간 이동({}초, {}km)은 정상으로 판단", timeDiffSeconds, distance);
            return 0;
        }

        // === 6단계: 속도 계산 및 분류 ===
        double speed = calculateSpeed(distance, timeDiffSeconds);

        if (speed < 0) {
            log.warn("속도 계산 불가: 시간차={}초, 거리={}km", timeDiffSeconds, distance);
            return 0;
        }

        double adjustedSuspiciousThreshold = calculateAdjustedThreshold(timeDiffSeconds);

        log.debug("계산된 속도: {}km/h (조정된 의심 임계값: {}km/h)", speed, adjustedSuspiciousThreshold);

        if (speed >= EXTREME_SPEED_KMH) {
            log.warn("즉시 차단급 이동 감지: {}km/h", speed);
            return 3;
        } else if (speed >= HIGH_SPEED_KMH) {
            log.warn("강한 의심 이동 감지: {}km/h", speed);
            return 2;
        } else if (speed >= adjustedSuspiciousThreshold) {
            log.info("의심 이동 감지: {}km/h", speed);
            return 1;
        }

        return 0;
    }

    // 시간 간격에 따른 임계값 계산
    private static double calculateAdjustedThreshold(long timeDiffSeconds) {
        if (timeDiffSeconds < 300) {        // 5분 미만
            return 80.0;
        } else if (timeDiffSeconds < 600) { // 10분 미만
            return 100.0;
        } else if (timeDiffSeconds < 1800) { // 30분 미만
            return 120.0;
        } else if (timeDiffSeconds < 3600) { // 1시간 미만
            return 140.0;
        } else {                            // 1시간 이상
            return 150.0;
        }
    }

    /**
     * 서버 시간과 클라이언트 시간의 차이가 허용 범위 내인지 검증
     * 클라이언트가 시간을 조작했을 가능성을 감지
     * @return 시간 조작 의심이면 true, 아니면 false
     */
    public static boolean isTimeManipulation(LocalDateTime serverTime, LocalDateTime clientTime) {
        if (clientTime == null) {
            return false; // 클라이언트 시간이 없으면 검증 불가
        }

        long diffMinutes = Math.abs(Duration.between(serverTime, clientTime).toMinutes());

        if (diffMinutes > TIME_SYNC_TOLERANCE_MINUTES) {
            log.warn("시간 조작 의심: 서버-클라이언트 시간차 = {}분", diffMinutes);
            return true;
        }

        return false;
    }

    /**
     * GPS 좌표의 유효성을 검증
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValidCoordinate(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        boolean isValid = latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;

        if (!isValid) {
            log.warn("유효하지 않은 좌표: lat={}, lng={}", latitude, longitude);
        }

        return isValid;
    }
}
