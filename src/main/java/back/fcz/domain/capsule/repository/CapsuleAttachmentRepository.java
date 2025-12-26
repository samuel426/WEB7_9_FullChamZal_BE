package back.fcz.domain.capsule.repository;

import back.fcz.domain.capsule.entity.CapsuleAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CapsuleAttachmentRepository extends JpaRepository<CapsuleAttachment,Long> {
    Optional<CapsuleAttachment> findByIdAndUploaderId(Long id, Long uploaderId);
}
