package back.fcz.domain.capsule.DTO.response;

import back.fcz.domain.capsule.DTO.UnlockDTO;
import back.fcz.domain.capsule.DTO.letterCustomDTO;

public record CapsuleCreateResponseDTO(
        Long memberId,
        Long capsuleId,
        String nickName,
        String title,
        String content,
        String visibility,

        UnlockDTO unlock,
        letterCustomDTO letter,

        int maxViewCount,
        int currentViewCount
){ }
