package back.fcz.domain.storytrack.repository;

import back.fcz.domain.storytrack.entity.StorytrackAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StorytrackAttachmentRepository extends JpaRepository<StorytrackAttachment, Long> {
    List<StorytrackAttachment> findByStorytrack_StorytrackIdAndDeletedAtIsNull(Long storytrackId);
}
