package back.fcz.domain.capsule.DTO.request;

import java.time.LocalDateTime;

public record CapsuleCreateRequestDTO (
        Long memberId,
        String nickName,
        String title,
        String content,
        String visibility,
        String unlockType,
        LocalDateTime unlockAt,
        String locationName,
        double locationLat,
        double locationIng,
        int viewingRadius,
        String pakcingColor,
        String contetnColor,
        int maxViewCount
){
    // 유효성 검사를 추가하기
}
