package back.fcz.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "nickname_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Builder
    private NicknameHistory(Long memberId, LocalDateTime changedAt) {
        this.memberId = memberId;
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    }
}
