package back.fcz.domain.storytrack.dto;

import back.fcz.domain.capsule.DTO.UnlockResponseDTO;

public record CapsuleResponse(
        Long capsuleId,
        String capsuleTitle,
        String capsuleContent,
        String unlockType,
        UnlockResponseDTO unlock
) {}
