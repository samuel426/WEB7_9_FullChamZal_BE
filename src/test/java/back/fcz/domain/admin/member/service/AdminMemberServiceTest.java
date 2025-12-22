//package back.fcz.domain.admin.member.service;
//
//import back.fcz.domain.admin.member.dto.AdminMemberStatusUpdateRequest;
//import back.fcz.domain.member.entity.Member;
//import back.fcz.domain.member.entity.MemberStatus;
//import back.fcz.domain.member.repository.MemberRepository;
//import back.fcz.domain.sanction.repository.MemberSanctionHistoryRepository;
//import back.fcz.global.auth.CurrentUserContext;
//import back.fcz.global.exception.BusinessException;
//import back.fcz.global.exception.ErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class AdminMemberServiceTest {
//
//    @Mock MemberRepository memberRepository;
//    @Mock MemberSanctionHistoryRepository memberSanctionHistoryRepository;
//    @Mock CurrentUserContext currentUserContext;
//
//    AdminMemberService service;
//
//    @BeforeEach
//    void setUp() {
//        service = new AdminMemberService(memberRepository, memberSanctionHistoryRepository, currentUserContext);
//    }
//
//    @Test
//    void 자기자신_상태변경_금지() {
//        when(currentUserContext.getCurrentMemberId()).thenReturn(1L);
//
//        AdminMemberStatusUpdateRequest req = new AdminMemberStatusUpdateRequest();
//        ReflectionTestUtils.setField(req, "status", MemberStatus.STOP);
//        ReflectionTestUtils.setField(req, "reason", "x");
//
//        BusinessException ex = catchThrowableOfType(() -> service.updateMemberStatus(1L, req), BusinessException.class);
//        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ADMIN_CANNOT_CHANGE_SELF_STATUS);
//    }
//
//    @Test
//    void EXIT상태_회원은_변경불가() {
//        when(currentUserContext.getCurrentMemberId()).thenReturn(999L);
//
//        Member target = Member.testMember(2L, "u", "n");
//        target.updateStatus(MemberStatus.EXIT);
//
//        when(memberRepository.findById(2L)).thenReturn(Optional.of(target));
//
//        AdminMemberStatusUpdateRequest req = new AdminMemberStatusUpdateRequest();
//        ReflectionTestUtils.setField(req, "status", MemberStatus.ACTIVE);
//
//        BusinessException ex = catchThrowableOfType(() -> service.updateMemberStatus(2L, req), BusinessException.class);
//        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ADMIN_INVALID_MEMBER_STATUS_CHANGE);
//    }
//
//    @Test
//    void STOP으로_변경하면_제재로그_저장되고_회원상태가_STOP() {
//        when(currentUserContext.getCurrentMemberId()).thenReturn(999L);
//
//        Member target = Member.testMember(2L, "u", "n");
//        when(memberRepository.findById(2L)).thenReturn(Optional.of(target));
//
//        AdminMemberStatusUpdateRequest req = new AdminMemberStatusUpdateRequest();
//        ReflectionTestUtils.setField(req, "status", MemberStatus.STOP);
//        ReflectionTestUtils.setField(req, "reason", "bad user");
//
//        service.updateMemberStatus(2L, req);
//
//        assertThat(target.getStatus()).isEqualTo(MemberStatus.STOP);
//
//        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
//        verify(memberSanctionHistoryRepository).save(captor.capture());
//        assertThat(captor.getValue()).isNotNull();
//    }
//}
