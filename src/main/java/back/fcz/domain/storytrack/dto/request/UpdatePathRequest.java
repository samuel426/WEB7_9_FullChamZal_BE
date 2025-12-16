package back.fcz.domain.storytrack.dto.request;

public record UpdatePathRequest(
        Long storytrackId,
        int updateStep,
        Long updatedCapsuleId
){
}
