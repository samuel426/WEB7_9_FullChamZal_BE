package back.fcz.domain.storytrack.dto.response;

import back.fcz.domain.storytrack.dto.CapsuleResponse;

public record UpdatePathResponse(
        Long storytrackId,
        int updatedStepOrder,
        CapsuleResponse capsule
){
}
