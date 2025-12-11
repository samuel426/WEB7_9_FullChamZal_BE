package back.fcz.domain.member.controller;

import back.fcz.domain.member.dto.request.PasswordVerifyRequest;
import back.fcz.domain.member.dto.response.MemberDetailResponse;
import back.fcz.domain.member.dto.response.MemberInfoResponse;
import back.fcz.domain.member.service.CurrentUserContext;
import back.fcz.domain.member.service.MemberService;
import back.fcz.global.config.swagger.ApiErrorCodeExample;
import back.fcz.global.dto.InServerMemberResponse;
import back.fcz.global.exception.ErrorCode;
import back.fcz.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "비밀번호 검증", description = "현재 로그인한 회원의 비밀번호를 검증합니다. 회원 정보 수정, 탈퇴 등 민감한 작업 전에 사용합니다.")
    @ApiErrorCodeExample({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.MEMBER_NOT_ACTIVE,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.TOKEN_EXPIRED,
            ErrorCode.TOKEN_INVALID,
            ErrorCode.INVALID_PASSWORD,
    })
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(
            @Valid @RequestBody PasswordVerifyRequest request
            ) {
        InServerMemberResponse user = currentUserContext.getCurrentUser();
        memberService.verifyPassword(user, request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "회원 정보", description = "회원 정보를 반환하는 API입니다. (전화번호는 마스킹 처리)")
    @ApiErrorCodeExample({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.MEMBER_NOT_ACTIVE,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.TOKEN_EXPIRED,
            ErrorCode.TOKEN_INVALID
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMe() {
        InServerMemberResponse user = currentUserContext.getCurrentUser();
        MemberInfoResponse response = memberService.getMe(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원 정보", description = "회원 정보 원본을 반환하는 API입니다.")
    @ApiErrorCodeExample({
            ErrorCode.MEMBER_NOT_FOUND,
            ErrorCode.MEMBER_NOT_ACTIVE,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.TOKEN_EXPIRED,
            ErrorCode.TOKEN_INVALID
    })
    @GetMapping("/me/detail")
    public ResponseEntity<ApiResponse<MemberDetailResponse>> getDetailMe() {
        InServerMemberResponse user = currentUserContext.getCurrentUser();
        MemberDetailResponse response = memberService.getDetailMe(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
