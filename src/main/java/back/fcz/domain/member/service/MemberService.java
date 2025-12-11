package back.fcz.domain.member.service;

import back.fcz.domain.member.dto.response.MemberInfoResponse;
import back.fcz.domain.member.entity.Member;
import back.fcz.domain.member.repository.MemberRepository;
import back.fcz.domain.member.util.PhoneMaskingUtil;
import back.fcz.global.crypto.PhoneCrypto;
import back.fcz.global.dto.InServerMemberResponse;
import back.fcz.global.exception.BusinessException;
import back.fcz.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PhoneCrypto phoneCrypto;

    public MemberInfoResponse getMe(InServerMemberResponse user) {
        Member member = memberRepository.findById(user.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String decryptedPhone = phoneCrypto.decrypt(member.getPhoneNumber());
        String maskedPhone = PhoneMaskingUtil.mask(decryptedPhone);

        return MemberInfoResponse.of(member, maskedPhone);
    }
}
