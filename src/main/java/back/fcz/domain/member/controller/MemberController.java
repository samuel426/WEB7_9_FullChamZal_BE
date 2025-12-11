package back.fcz.domain.member.controller;

import back.fcz.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

//    @Operation(summary = "회원 정보", description = "회원 정보를 반환하는 API입니다. (전화번호는 마스킹 처리)")
//    @ApiErrorCodeExample({
//            ErrorCode.DUPLICATE_USER_ID,
//            ErrorCode.DUPLICATE_NICKNAME,
//            ErrorCode.DUPLICATE_PHONENUM
//    })
//    @PostMapping("/me")
//    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMe(
//
//    ) {
//        MemberInfoResponse response = memberService.getMe(request);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
}
