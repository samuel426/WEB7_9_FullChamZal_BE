package back.fcz.global.init;

import back.fcz.domain.capsule.entity.Capsule;
import back.fcz.domain.capsule.repository.CapsuleRepository;
import back.fcz.domain.member.entity.Member;
import back.fcz.domain.member.entity.MemberRole;
import back.fcz.domain.member.entity.MemberStatus;
import back.fcz.domain.member.repository.MemberRepository;
import back.fcz.domain.report.entity.Report;
import back.fcz.domain.report.entity.ReportReasonType;
import back.fcz.domain.report.entity.ReportStatus;
import back.fcz.domain.report.repository.ReportRepository;
import back.fcz.domain.storytrack.entity.Storytrack;
import back.fcz.domain.storytrack.entity.StorytrackStep;
import back.fcz.domain.storytrack.repository.StorytrackRepository;
import back.fcz.global.crypto.PhoneCrypto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoInitData implements CommandLineRunner {

    private static final String DEMO_CAPSULE_PREFIX = "[DEMO]";
    private static final String DEMO_STORYTRACK_TITLE = "[DEMO] 서울의 기억 산책";

    private final MemberRepository memberRepository;
    private final CapsuleRepository capsuleRepository;
    private final ReportRepository reportRepository;
    private final StorytrackRepository storytrackRepository;
    private final PhoneCrypto phoneCrypto;
    private final PasswordEncoder passwordEncoder;

    @Value("${demo.user-password}")
    private String demoUserPassword;

    @Value("${demo.admin-password}")
    private String demoAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        Member demoUser = getOrCreateMember(
                "demo",
                demoUserPassword,
                "데모 사용자",
                "기억여행자",
                "01000000001",
                MemberRole.USER
        );
        getOrCreateMember(
                "admin",
                demoAdminPassword,
                "데모 관리자",
                "관리자",
                "01000000002",
                MemberRole.ADMIN
        );

        List<Capsule> demoCapsules = findDemoCapsules();
        if (demoCapsules.isEmpty()) {
            demoCapsules = createDemoCapsules(demoUser);
        }

        createDemoReportIfMissing(demoUser, demoCapsules.get(0));
        createDemoStorytrackIfMissing(demoUser, demoCapsules);

        log.info("Demo data is ready (userId=demo, adminId=admin)");
    }

    private Member getOrCreateMember(
            String userId,
            String password,
            String name,
            String nickname,
            String phone,
            MemberRole role
    ) {
        return memberRepository.findByUserId(userId)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .userId(userId)
                                .passwordHash(passwordEncoder.encode(password))
                                .name(name)
                                .nickname(nickname)
                                .phoneNumber(phoneCrypto.encrypt(phone))
                                .phoneHash(phoneCrypto.hash(phone))
                                .status(MemberStatus.ACTIVE)
                                .role(role)
                                .build()
                ));
    }

    private List<Capsule> findDemoCapsules() {
        return capsuleRepository.findAll().stream()
                .filter(capsule -> capsule.getTitle() != null)
                .filter(capsule -> capsule.getTitle().startsWith(DEMO_CAPSULE_PREFIX))
                .sorted(Comparator.comparing(Capsule::getCapsuleId))
                .toList();
    }

    private List<Capsule> createDemoCapsules(Member member) {
        LocalDateTime now = LocalDateTime.now();

        Capsule opened = buildCapsule(
                member,
                "[DEMO] 1년 전의 나에게",
                "오늘의 선택과 고민도 언젠가 소중한 기억이 됩니다.",
                "TIME",
                now.minusDays(1),
                null,
                null,
                null,
                0
        );
        Capsule location = buildCapsule(
                member,
                "[DEMO] 서울광장에서 여는 편지",
                "서울광장에 도착하면 열 수 있는 위치 기반 캡슐입니다.",
                "LOCATION",
                null,
                "서울광장",
                37.5663,
                126.9779,
                150
        );
        Capsule combined = buildCapsule(
                member,
                "[DEMO] 남산에서 만나는 여름",
                "시간과 장소 조건을 함께 사용하는 공개 캡슐입니다.",
                "TIME_AND_LOCATION",
                now.minusHours(1),
                "남산서울타워",
                37.5512,
                126.9882,
                300
        );
        Capsule future = buildCapsule(
                member,
                "[DEMO] 다음 계절에 공개될 편지",
                "미래 시점에 열리도록 설정된 시간 기반 캡슐입니다.",
                "TIME",
                now.plusDays(30),
                null,
                null,
                null,
                0
        );

        return capsuleRepository.saveAll(List.of(opened, location, combined, future));
    }

    private Capsule buildCapsule(
            Member member,
            String title,
            String content,
            String unlockType,
            LocalDateTime unlockAt,
            String locationName,
            Double latitude,
            Double longitude,
            int radius
    ) {
        return Capsule.builder()
                .memberId(member)
                .uuid(UUID.randomUUID().toString())
                .nickname(member.getNickname())
                .title(title)
                .content(content)
                .capsuleColor("WHITE")
                .capsulePackingColor("BLUE")
                .visibility("PUBLIC")
                .unlockType(unlockType)
                .unlockAt(unlockAt)
                .locationName(locationName)
                .locationLat(latitude)
                .locationLng(longitude)
                .locationRadiusM(radius)
                .maxViewCount(0)
                .currentViewCount(0)
                .isProtected(0)
                .isDeleted(0)
                .likeCount(0)
                .build();
    }

    private void createDemoReportIfMissing(Member reporter, Capsule capsule) {
        if (reportRepository.count() > 0) {
            return;
        }

        reportRepository.save(
                Report.builder()
                        .capsule(capsule)
                        .reporter(reporter)
                        .reasonType(ReportReasonType.SPAM)
                        .reasonDetail("관리자 신고 처리 화면을 확인하기 위한 데모 신고입니다.")
                        .status(ReportStatus.PENDING)
                        .build()
        );
    }

    private void createDemoStorytrackIfMissing(Member creator, List<Capsule> capsules) {
        boolean exists = storytrackRepository.findAll().stream()
                .anyMatch(storytrack -> DEMO_STORYTRACK_TITLE.equals(storytrack.getTitle()));
        if (exists) {
            return;
        }

        Storytrack storytrack = Storytrack.builder()
                .member(creator)
                .title(DEMO_STORYTRACK_TITLE)
                .description("서울의 장소를 따라 공개 캡슐을 확인하는 데모 스토리트랙입니다.")
                .trackType("SEQUENTIAL")
                .isPublic(1)
                .price(0)
                .totalSteps(0)
                .isDeleted(0)
                .build();

        int stepOrder = 1;
        for (Capsule capsule : capsules.stream().limit(3).toList()) {
            storytrack.addStep(
                    StorytrackStep.builder()
                            .capsule(capsule)
                            .stepOrder(stepOrder++)
                            .build()
            );
        }
        storytrack.setTotalSteps(storytrack.getSteps().size());
        storytrackRepository.save(storytrack);
    }
}
