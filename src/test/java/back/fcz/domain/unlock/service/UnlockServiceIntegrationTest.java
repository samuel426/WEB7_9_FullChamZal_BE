package back.fcz.domain.unlock.service;

import back.fcz.domain.capsule.entity.AnomalyType;
import back.fcz.domain.capsule.entity.Capsule;
import back.fcz.domain.capsule.entity.CapsuleOpenLog;
import back.fcz.domain.capsule.entity.CapsuleOpenStatus;
import back.fcz.domain.capsule.repository.CapsuleOpenLogRepository;
import back.fcz.domain.capsule.repository.CapsuleRepository;
import back.fcz.domain.member.entity.Member;
import back.fcz.domain.member.entity.MemberRole;
import back.fcz.domain.member.entity.MemberStatus;
import back.fcz.domain.member.repository.MemberRepository;
import back.fcz.domain.sanction.constant.SanctionConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UnlockServiceIntegrationTest {

    @Autowired
    private UnlockService unlockService;

    @Autowired
    private CapsuleRepository capsuleRepository;

    @Autowired
    private CapsuleOpenLogRepository capsuleOpenLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SanctionConstants sanctionConstants;

    @Test
    @DisplayName("24시간 윈도우 - 이전 로그가 24시간 이내면 조회되어 이동 분석 수행")
    void testLogWindowWithin24Hours() {
        // Given: 회원 생성
        Member member = createTestMember();
        memberRepository.save(member);

        // Given: 위치 기반 캡슐 생성
        Capsule capsule = createLocationCapsule(member);
        capsuleRepository.save(capsule);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyThreeHoursAgo = now.minusHours(23);

        // Given: 23시간 전 서울에서 시도한 로그
        CapsuleOpenLog oldLog = CapsuleOpenLog.builder()
                .capsuleId(capsule)
                .memberId(member.getMemberId())
                .ipAddress("127.0.0.1")
                .viewerType("MEMBER")
                .openedAt(twentyThreeHoursAgo)
                .currentLat(37.5665)  // 서울
                .currentLng(126.9780)
                .status(CapsuleOpenStatus.FAIL_LOCATION)
                .anomalyType(AnomalyType.NONE)
                .build();
        capsuleOpenLogRepository.save(oldLog);

        // When: 23시간 후 50m 이동하여 시도 (GPS 오차 범위)
        var result = unlockService.validateTimeAndLocationConditions(
                capsule, now, 37.5670, 126.9785, now,
                member.getMemberId(), "127.0.0.1"
        );

        // Then: 이전 로그가 조회되었지만 GPS 오차 범위로 정상 처리
        assertNotNull(result);
        assertEquals(AnomalyType.NONE, result.getAnomalyType());
    }

    @Test
    @DisplayName("24시간 윈도우 - 25시간 전 로그는 조회되지 않음")
    void testLogWindowBeyond24Hours() {
        // Given: 회원 생성
        Member member = createTestMember();
        memberRepository.save(member);

        // Given: 위치 기반 캡슐 생성
        Capsule capsule = createLocationCapsule(member);
        capsuleRepository.save(capsule);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyFiveHoursAgo = now.minusHours(25);

        // Given: 25시간 전 서울에서 시도한 로그
        CapsuleOpenLog oldLog = CapsuleOpenLog.builder()
                .capsuleId(capsule)
                .memberId(member.getMemberId())
                .ipAddress("127.0.0.1")
                .viewerType("MEMBER")
                .openedAt(twentyFiveHoursAgo)
                .currentLat(37.5665)  // 서울
                .currentLng(126.9780)
                .status(CapsuleOpenStatus.FAIL_LOCATION)
                .anomalyType(AnomalyType.NONE)
                .build();
        capsuleOpenLogRepository.save(oldLog);

        // When: 25시간 후 부산에서 시도 (정상적으로 가능한 이동)
        var result = unlockService.validateTimeAndLocationConditions(
                capsule, now, 35.1796, 129.0756, now,
                member.getMemberId(), "127.0.0.1"
        );

        // Then: 25시간 전 로그는 윈도우 밖이므로 첫 시도로 간주하여 정상 처리
        assertNotNull(result);
        assertEquals(AnomalyType.NONE, result.getAnomalyType());
    }

    @Test
    @DisplayName("중복 요청 필터링 - 3초 이내 시도는 이동 분석 스킵")
    void testDuplicateRequestFiltering() {
        // Given: 회원 생성
        Member member = createTestMember();
        memberRepository.save(member);

        // Given: 위치 기반 캡슐 생성
        Capsule capsule = createLocationCapsule(member);
        capsuleRepository.save(capsule);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoSecondsAgo = now.minusSeconds(2);

        // Given: 2초 전 서울에서 시도한 로그
        CapsuleOpenLog recentLog = CapsuleOpenLog.builder()
                .capsuleId(capsule)
                .memberId(member.getMemberId())
                .ipAddress("127.0.0.1")
                .viewerType("MEMBER")
                .openedAt(twoSecondsAgo)
                .currentLat(37.5665)  // 서울
                .currentLng(126.9780)
                .status(CapsuleOpenStatus.FAIL_LOCATION)
                .anomalyType(AnomalyType.NONE)
                .build();
        capsuleOpenLogRepository.save(recentLog);

        // When: 2초 후 부산에서 시도 (불가능한 이동이지만 중복 요청)
        var result = unlockService.validateTimeAndLocationConditions(
                capsule, now, 35.1796, 129.0756, now,
                member.getMemberId(), "127.0.0.1"
        );

        // Then: 3초 이내이므로 중복 요청으로 간주하여 정상 처리
        assertNotNull(result);
        assertEquals(AnomalyType.NONE, result.getAnomalyType());
    }

    @Test
    @DisplayName("이상 이동 감지 - 10초 만에 서울->부산 이동은 IMPOSSIBLE_MOVEMENT")
    void testImpossibleMovementDetection() {
        // Given: 회원 생성
        Member member = createTestMember();
        memberRepository.saveAndFlush(member);

        // Given: 위치 기반 캡슐 생성
        Capsule capsule = createLocationCapsule(member);
        capsuleRepository.saveAndFlush(capsule);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenSecondsAgo = now.minusSeconds(10);

        System.out.println("=== 테스트 시간 정보 ===");
        System.out.println("now: " + now);
        System.out.println("tenSecondsAgo: " + tenSecondsAgo);
        System.out.println("시간 차이(초): " + Duration.between(tenSecondsAgo, now).getSeconds());

        // Given: 10초 전 서울에서 시도한 로그
        CapsuleOpenLog recentLog = CapsuleOpenLog.builder()
                .capsuleId(capsule)
                .memberId(member.getMemberId())
                .ipAddress("127.0.0.1")
                .viewerType("MEMBER")
                .openedAt(tenSecondsAgo)
                .currentLat(37.5665)  // 서울
                .currentLng(126.9780)
                .status(CapsuleOpenStatus.FAIL_LOCATION)
                .anomalyType(AnomalyType.NONE)
                .build();
        capsuleOpenLogRepository.saveAndFlush(recentLog);

        // When
        var result = unlockService.validateTimeAndLocationConditions(
                capsule, now, 35.1796, 129.0756, now,
                member.getMemberId(), "127.0.0.1"
        );

        // Then
        System.out.println("=== 결과 AnomalyType: " + result.getAnomalyType());
        System.out.println("=== 결과 SuspicionScore: " + result.getSuspicionScore());

        assertNotNull(result);
        assertEquals(AnomalyType.IMPOSSIBLE_MOVEMENT, result.getAnomalyType());
        assertTrue(result.getSuspicionScore() > 0);
    }

    @Test
    @DisplayName("정상 이동 - 30분 만에 50km 이동은 정상 (100km/h)")
    void testNormalMovement() {
        // Given: 회원 생성
        Member member = createTestMember();
        memberRepository.save(member);

        // Given: 위치 기반 캡슐 생성
        Capsule capsule = createLocationCapsule(member);
        capsuleRepository.save(capsule);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyMinutesAgo = now.minusMinutes(30);

        // Given: 30분 전 서울에서 시도한 로그
        CapsuleOpenLog oldLog = CapsuleOpenLog.builder()
                .capsuleId(capsule)
                .memberId(member.getMemberId())
                .ipAddress("127.0.0.1")
                .viewerType("MEMBER")
                .openedAt(thirtyMinutesAgo)
                .currentLat(37.5665)  // 서울 시청
                .currentLng(126.9780)
                .status(CapsuleOpenStatus.FAIL_LOCATION)
                .anomalyType(AnomalyType.NONE)
                .build();
        capsuleOpenLogRepository.save(oldLog);

        // When: 30분 후 인천공항에서 시도 (약 50km)
        var result = unlockService.validateTimeAndLocationConditions(
                capsule, now, 37.4563, 126.7052, now,
                member.getMemberId(), "127.0.0.1"
        );

        // Then: 정상 이동 속도로 판단
        assertNotNull(result);
        assertEquals(AnomalyType.NONE, result.getAnomalyType());
    }

    @Test
    @DisplayName("비회원 IP 기반 로그 조회 테스트")
    void testGuestIpBasedLogRetrieval() {
        // Given: 비회원용 위치 기반 캡슐 생성
        Member owner = createTestMember();
        memberRepository.save(owner);

        Capsule capsule = createLocationCapsule(owner);
        capsuleRepository.save(capsule);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);

        // Given: 5분 전 같은 IP에서 시도한 비회원 로그
        CapsuleOpenLog guestLog = CapsuleOpenLog.builder()
                .capsuleId(capsule)
                .ipAddress("192.168.1.100")
                .viewerType("GUEST_UNAUTH")
                .openedAt(fiveMinutesAgo)
                .currentLat(37.5665)
                .currentLng(126.9780)
                .status(CapsuleOpenStatus.FAIL_LOCATION)
                .anomalyType(AnomalyType.NONE)
                .build();
        capsuleOpenLogRepository.save(guestLog);

        // When: 같은 IP에서 비회원이 시도 (50m 이동)
        var result = unlockService.validateTimeAndLocationConditions(
                capsule, now, 37.5670, 126.9785, now,
                null, "192.168.1.100"
        );

        // Then: IP 기반으로 이전 로그를 찾아서 정상 처리
        assertNotNull(result);
        assertEquals(AnomalyType.NONE, result.getAnomalyType());
    }

    // === Helper Methods ===

    private Member createTestMember() {
        return Member.builder()
                .userId("test-user")
                .passwordHash("test-password-hash")
                .name("테스트유저")
                .nickname("테스터")
                .phoneNumber("encrypted-phone")
                .phoneHash("hashed-phone")
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .build();
    }

    private Capsule createLocationCapsule(Member owner) {
        return Capsule.builder()
                .memberId(owner)
                .uuid("test-uuid-" + System.currentTimeMillis())
                .nickname("테스터")
                .content("테스트 캡슐 내용")
                .capsuleColor("RED")
                .capsulePackingColor("BLUE")
                .visibility("PRIVATE")
                .unlockType("LOCATION")
                .locationLat(37.5665)  // 서울 시청
                .locationLng(126.9780)
                .locationRadiusM(500)
                .isProtected(0)
                .build();
    }
}