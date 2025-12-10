package back.fcz.domain.capsule.DTO.request;

import java.time.LocalDateTime;

public record SecretCapsuleCreateRequest (
        Long memberId,
        String nickName,
        String phoneNum,
        String title,
        String content,
        String visibility,
        String unlockType,
        LocalDateTime unlockAt,
        String locationName,
        double locationLat,
        double locationIng,
        int viewingRadius,
        String packingColor,
        String contentColor,
        int maxViewCount
) { }
