package back.fcz.domain.storytrack.dto;

import back.fcz.domain.storytrack.dto.response.createrStorytrackListResponse;

import java.util.List;

record createrStorytrackListWrapper(
        List<createrStorytrackListResponse> storytracks
) {}
