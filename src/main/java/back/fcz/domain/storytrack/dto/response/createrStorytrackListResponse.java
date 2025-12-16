package back.fcz.domain.storytrack.dto.response;

import java.time.LocalDateTime;

public record createrStorytrackListResponse (
        Long storytrackId,
        String title,
        String description,
        String trackType,
        int isPublic,
        int price,
        int totalSteps,
        LocalDateTime createAt
){
}
