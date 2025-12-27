package back.fcz.domain.storytrack.dto.response;


public record TotalStorytrackResponse(
        Long storytrackId,
        String createrName,
        String title,
        String desctiption,
        String trackType,
        int isPublic,
        int price,
        int totalSteps,
        Long totalMemberCount
){ }
