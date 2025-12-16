package back.fcz.domain.storytrack.dto;

import back.fcz.domain.storytrack.dto.response.participantStorytrackListResponse;

import java.util.List;

record participantStorytrackListWrapper(
        List<participantStorytrackListResponse> storytracks
) {}
