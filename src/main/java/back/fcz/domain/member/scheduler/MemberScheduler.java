package back.fcz.domain.member.scheduler;

import back.fcz.domain.member.entity.Member;
import back.fcz.domain.member.entity.MemberStatus;
import back.fcz.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberScheduler {

    private final MemberRepository memberRepository;

    private static final int ANONYMIZE_DAYS_AFTER_DELETION = 30;

    // 매일 새벽 3시, 탈퇴 후 30일이 지난 회원의 개인정보 익명화
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void analymizeDeletedMembers() {
        LocalDateTime targetDate = LocalDateTime.now()
                .minusDays(ANONYMIZE_DAYS_AFTER_DELETION);

        List<Member> membersToAnonymize = memberRepository
                .findAllByStatusAndDeletedAtBefore(MemberStatus.EXIT, targetDate);

        if (membersToAnonymize.isEmpty()) {
            log.info("익명화 대상 회원 없음");
            return;
        }

        int count = 0;
        for (Member member : membersToAnonymize) {
            member.anonymize();
            count++;
            log.info("회원 개인정보 익명화 완료 - memberId: {}", member.getMemberId());
        }
    }
}
