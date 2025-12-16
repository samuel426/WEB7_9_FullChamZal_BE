package back.fcz.domain.storytrack.dto.response;

import back.fcz.domain.storytrack.dto.pathResponse;

import java.time.LocalDateTime;
import java.util.List;

record storytrackDashboardResponse (
        Long storytrackId,
        String title,
        String storytrackType,
        int isPublic,
        int totalSteps,
        LocalDateTime createdAt,
        int totalParticipant,
        int completeParticipant,
        List<pathResponse> paths
){
}
