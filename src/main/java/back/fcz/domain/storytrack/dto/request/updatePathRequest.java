package back.fcz.domain.storytrack.dto.request;

public record updatePathRequest (
        Long storytrackId,
        int updateStep,
        Long updatedCapsuleId
){
}
