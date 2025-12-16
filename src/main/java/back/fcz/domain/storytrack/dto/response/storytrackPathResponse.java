package back.fcz.domain.storytrack.dto.response;

import back.fcz.domain.storytrack.dto.pathResponse;

import java.util.List;

public record storytrackPathResponse(
        Long storytrackId,
        String title,
        String description,
        int totalStep,
        List<pathResponse> paths
) {}
