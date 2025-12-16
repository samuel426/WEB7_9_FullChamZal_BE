package back.fcz.domain.capsule.service;

import back.fcz.domain.capsule.DTO.request.CapsuleSaveButtonRequest;
import back.fcz.domain.capsule.DTO.response.CapsuleSaveButtonResponse;
import back.fcz.domain.capsule.entity.Capsule;
import back.fcz.domain.capsule.entity.CapsuleRecipient;
import back.fcz.domain.capsule.repository.CapsuleRecipientRepository;
import back.fcz.domain.capsule.repository.CapsuleRepository;
import back.fcz.domain.member.entity.Member;
import back.fcz.domain.member.repository.MemberRepository;
import back.fcz.domain.member.service.CurrentUserContext;
import back.fcz.global.exception.BusinessException;
import back.fcz.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CapsuleSaveButtonService {
    private final CurrentUserContext currentUserContext;
    private final MemberRepository memberRepository;
    private final CapsuleRepository  capsuleRepository;
    private final CapsuleRecipientRepository capsuleRecipientRepository;

    public CapsuleSaveButtonResponse saveRecipient(CapsuleSaveButtonRequest request, Long currentMemberId) {

        //로그인 상태라면 개인 캡슐 수신자 정보 생성(현재 로그인 중인 회원의 데이터 기록)
        Member member = memberRepository.findById(currentMemberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Capsule capsule = capsuleRepository.findById(request.capsuleId()).orElseThrow(() -> new BusinessException(ErrorCode.CAPSULE_NOT_FOUND));
        CapsuleRecipient capsuleRecipient = CapsuleRecipient.builder()
                .capsuleId(capsule)
                .recipientName(member.getNickname())
                .recipientPhone(member.getPhoneNumber())
                .recipientPhoneHash(member.getPhoneHash())
                .isSenderSelf(request.isSendSelf())
                .unlockedAt(request.unlockAt()) // Dto에서 받아오는게 나을듯
                .build();

        capsuleRecipientRepository.save(capsuleRecipient);
        return new CapsuleSaveButtonResponse("캡슐이 저장 되었습니다.");
    }

    public Long loginCheck(){
        Long currentMemberId = currentUserContext.getCurrentMemberId();
        if(currentMemberId != null){
            return currentMemberId;
        }else{
            return null;
        }
    }
}
