package back.fcz.domain.openai.moderation.repository;

import back.fcz.domain.openai.moderation.entity.ModerationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ModerationAuditLogRepository extends JpaRepository<ModerationAuditLog, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update ModerationAuditLog m set m.capsuleId = :capsuleId where m.id = :auditId")
    int attachCapsuleId(Long auditId, Long capsuleId);
}
