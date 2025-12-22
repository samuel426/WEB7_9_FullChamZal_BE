package back.fcz.domain.member.dto.request;

public record MemberLoginPwRequest(
        String phoneNum,
        String password
) {
}
