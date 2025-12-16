package back.fcz.domain.storytrack.dto.response;

import java.time.LocalDateTime;

record participantProgressReponse (
        Long storytrackId,
        int completedSteps,
        int lastCompletedStep,
        LocalDateTime completedAt,
        LocalDateTime createdAt
){
}
