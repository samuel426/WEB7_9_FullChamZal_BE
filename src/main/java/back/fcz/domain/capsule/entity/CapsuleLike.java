package back.fcz.domain.capsule.entity;

/*

@Entity
@Builder
@Table(
        name = "capsule_like",
        uniqueConstraints = {
                // 복합 키로 "중복 좋아요"를 DB레벨에서 막음
                @UniqueConstraint(
                        name = "uc_member_capsule",
                        columnNames = {"member_id", "capsule_id"}
                )
        }
)
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class CapsuleLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capsule_id", nullable = false)
    private Capsule capsuleId;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
*/
