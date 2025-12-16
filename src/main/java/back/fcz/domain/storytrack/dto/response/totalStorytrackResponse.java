package back.fcz.domain.storytrack.dto.response;


record totalStorytrackResponse (
        Long storytrackId,
        String createName,
        String title,
        String desctiption,
        String trackType,
        int isPublic,
        int price,
        int totalSteps
){
}
