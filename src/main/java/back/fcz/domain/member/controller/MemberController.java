package back.fcz.domain.member.controller;

import back.fcz.domain.member.dto.response.MemberInfoResponse;
import back.fcz.domain.member.service.CurrentUserContext;
import back.fcz.domain.member.service.MemberService;
import back.fcz.global.config.swagger.ApiErrorCodeExample;
import back.fcz.global.dto.InServerMemberResponse;
import back.fcz.global.exception.ErrorCode;
import back.fcz.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(
        name = "회원 API",
        description = "회원 정보 조회/수정, 닉네임 변경 등 회원 관리 API"
)
public class MemberController {

    private final MemberService memberService;
    private final CurrentUserContext currentUserContext;

    @Operation(summary = "회원 정보", description = "회원 정보를 반환하는 API입니다. (전화번호는 마스킹 처리)")
    @ApiErrorCodeExample({
            ErrorCode.MEMBER_NOT_FOUND
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMe() {
        InServerMemberResponse user = currentUserContext.getCurrentUser();
        MemberInfoResponse response = memberService.getMe(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
