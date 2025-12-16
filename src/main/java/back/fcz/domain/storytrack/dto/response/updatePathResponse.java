package back.fcz.domain.storytrack.dto.response;

import back.fcz.domain.storytrack.dto.capsuleResponse;

public record updatePathResponse (
        Long storytrackId,
        int updatedStepOrder,
        capsuleResponse capsule
){
}
