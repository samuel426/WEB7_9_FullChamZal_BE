package back.fcz.domain.storytrack.dto.response;

import back.fcz.domain.storytrack.dto.PathResponse;
import back.fcz.global.dto.PageResponse;

public record StorytrackPathResponse(
        Long storytrackId,
        String title,
        String description,
        int totalStep,
        PageResponse<PathResponse> paths
) {}
