package back.fcz.domain.capsule.service;

/*

@Service
@RequiredArgsConstructor
public class CapsuleLikeService {
    private final CapsuleRepository capsuleRepository;
    private final CapsuleLikeRepository capsuleLikeRepository;
    private final MemberRepository  memberRepository;
    private final CurrentUserContext currentUserContext;

    public void likeUp(Long capsuleId) {
        Long memberId = currentUserContext.getCurrentMemberId();
        //중복된 요청인지 먼저 체크(같은 사용자가 같은 캡슐에 좋아요를 누르는 것을 방지)
        if(capsuleLikeRepository.existsByCapsuleIdMemberId(capsuleId, memberId)){
            throw new BusinessException(ErrorCode.DUPLICATE_LIKE_REQUEST);
        }
        

        
        
        //좋아요 객체 생성

        //capsule과 member를 진짜 DB조회하지말고 프록시 객체를 만들어서 ID값만 넣는 방식을 쓰면
        //실제 조회를 하는게 아니기 때문에 성능면에서 이점이 있다. 단점은 무엇 
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Capsule capsule = capsuleRepository.findById(capsuleId).orElseThrow(() -> new BusinessException(ErrorCode.CAPSULE_NOT_FOUND));
        CapsuleLike capsuleLike = CapsuleLike.builder()
                .capsuleId(capsule)
                .memberId(member)
                .build();
        capsuleLikeRepository.save(capsuleLike);

        //해당 캡슐의 좋아요 값 +1( 이떄 그냥 단순히 capsule.likeCountUp() 메서드로 값을 올릴지
        // 아니면 DB를 한번 읽어와서 있는 엔티티 갯수에 +1한 값을 세팅할지 고민해봐야함)
    }


    public void likeDown(Long capsuleId) {
        Long memberId = currentUserContext.getCurrentMemberId();
        //좋아요 객체 삭제

        //해당 캡슐의 좋아요 값 -1
    }
}
*/
