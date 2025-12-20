package back.fcz.domain.openai.moderation.entity;

import back.fcz.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "moderation_audit_log")
public class ModerationAuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "capsule_id")
    private Long capsuleId;

    @Column(name = "capsule_uuid", length = 36)
    private String capsuleUuid;

    @Column(name = "actor_member_id")
    private Long actorMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ModerationActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 30)
    private ModerationDecision decision;

    @Column(name = "model", nullable = false, length = 64)
    private String model;

    @Column(name = "input_hash", nullable = false, length = 64)
    private String inputHash;

    @Column(name = "input_preview", length = 1000)
    private String inputPreview;

    @Column(name = "flagged", nullable = false)
    private boolean flagged;

    @Lob
    @Column(name = "categories_json", columnDefinition = "TEXT")
    private String categoriesJson;

    @Lob
    @Column(name = "category_scores_json", columnDefinition = "TEXT")
    private String categoryScoresJson;

    @Column(name = "openai_moderation_id", length = 80)
    private String openaiModerationId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public void attachCapsuleId(Long capsuleId) {
        this.capsuleId = capsuleId;
    }
}
