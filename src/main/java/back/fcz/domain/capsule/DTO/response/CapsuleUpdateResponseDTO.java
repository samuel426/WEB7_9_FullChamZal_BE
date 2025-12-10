package back.fcz.domain.capsule.DTO.response;

import back.fcz.domain.capsule.DTO.UnlockDTO;
import back.fcz.domain.capsule.DTO.letterCustomDTO;

public record CapsuleUpdateResponseDTO(
        Long memberId,
        Long capsuleId,
        String nickName,
        String URL,
        String updatedTitle,
        String updatedContent,
        String visibility,
        String unlockType,
        UnlockDTO unlock,
        letterCustomDTO letter,
        int maxViewCount
){ }
