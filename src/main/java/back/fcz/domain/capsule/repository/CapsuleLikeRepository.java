package back.fcz.domain.capsule.repository;


import back.fcz.domain.capsule.entity.CapsuleLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CapsuleLikeRepository extends JpaRepository<CapsuleLike, Long> {
    @Query("SELECT COUNT(c) > 0 " +
            "FROM CapsuleLike c " +
            "WHERE c.capsuleId.capsuleId = :capsuleId " +
            "AND c.memberId.memberId = :memberId")
    boolean existsByCapsuleIdMemberId(@Param("capsuleId") Long capsuleId, @Param("memberId") Long memberId);

    void deleteByCapsuleId_CapsuleIdAndMemberId_MemberId(Long capsuleId, Long memberId);
}
