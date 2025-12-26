package back.fcz.domain.sanction.service;

import back.fcz.domain.member.entity.Member;
import back.fcz.domain.member.entity.MemberStatus;
import back.fcz.domain.member.repository.MemberRepository;
import back.fcz.domain.sanction.constant.SanctionConstants;
import back.fcz.domain.sanction.entity.MemberSanctionHistory;
import back.fcz.domain.sanction.entity.SanctionType;
import back.fcz.domain.sanction.repository.MemberSanctionHistoryRepository;
import back.fcz.global.exception.BusinessException;
import back.fcz.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 회원 제재 처리 서비스 (자동 제재/수동 제재)
@Slf4j
@Service
@RequiredArgsConstructor
public class SanctionService {

    private final MemberRepository memberRepository;
    private final MemberSanctionHistoryRepository sanctionHistoryRepository;
    private final SanctionConstants sanctionConstants;

    // 자동 정지 처리 수행
    @Transactional
    public void applyAutoSuspension(Long memberId, String reason, int days) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == MemberStatus.STOP) {
            log.warn("이미 정지된 회원: {}", memberId);
            return;
        }

        MemberStatus beforeStatus = member.getStatus();

        member.updateStatus(MemberStatus.STOP);
        memberRepository.save(member);

        LocalDateTime sanctionUntil = LocalDateTime.now().plusDays(days);

        recordAutoSanction(memberId, SanctionType.STOP, beforeStatus, reason, sanctionUntil);

        log.info("자동 정지 처리 완료: 회원 {} → {}일 정지 (해제 일시: {})",
                memberId, days, sanctionUntil);
    }

    // 자동 제재 이력 기록
    @Transactional
    public void recordAutoSanction(
            Long memberId,
            SanctionType sanctionType,
            MemberStatus beforeStatus,
            String reason,
            LocalDateTime sanctionUntil) {
        Long systemAdminId = sanctionConstants.getSystemAdminId();

        String fullReason = SanctionConstants.AUTO_SANCTION_REASON_PREFIX + reason;

        MemberStatus afterStatus = (sanctionType == SanctionType.STOP)
                ? MemberStatus.STOP
                : MemberStatus.ACTIVE;

        MemberSanctionHistory history = MemberSanctionHistory.create(
                memberId,
                systemAdminId,
                sanctionType,
                beforeStatus,
                afterStatus,
                fullReason,
                sanctionUntil
        );

        sanctionHistoryRepository.save(history);

        log.info("자동 제재 이력 기록 완료: 회원 {} (유형: {}, 사유: {})",
                memberId, sanctionType, reason);
    }

    // 임시 정지 해제
    @Transactional
    public void restoreSuspension(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() != MemberStatus.STOP) {
            log.warn("정지 상태가 아닌 회원입니다: {}", memberId);
            return;
        }

        MemberStatus beforeStatus = member.getStatus();

        member.updateStatus(MemberStatus.ACTIVE);

        member.updateStatus(MemberStatus.ACTIVE);
        memberRepository.save(member);

        recordAutoSanction(
                memberId,
                SanctionType.RESTORE,
                beforeStatus,
                "정지 기간 만료로 자동 복구",
                null
        );

        log.info("정지 해제 완료: 회원 {}", memberId);
    }
}
