package back.fcz.domain.storytrack.repository;

import back.fcz.domain.storytrack.entity.StorytrackStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorytrackStepRepository extends JpaRepository<StorytrackStep, Long> {
}
