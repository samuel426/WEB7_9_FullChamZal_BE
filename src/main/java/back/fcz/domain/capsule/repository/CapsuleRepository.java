package back.fcz.domain.capsule.repository;

import back.fcz.domain.capsule.entity.Capsule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapsuleRepository extends JpaRepository<Capsule, Long> {

    // 기본: 삭제되지 않은 캡슐만 조회
    Page<Capsule> findByIsDeletedFalse(Pageable pageable);

    // visibility 필터 (PUBLIC / PRIVATE)
    Page<Capsule> findByIsDeletedFalseAndVisibility(String visibility, Pageable pageable);

    // TODO: 작성자, 기간, 키워드 검색 등은 추후 QueryDsl / Specification 으로 확장
}
