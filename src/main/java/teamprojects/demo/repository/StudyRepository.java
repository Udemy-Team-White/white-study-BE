package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamprojects.demo.entity.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyRepository extends JpaRepository<Study, Integer>, JpaSpecificationExecutor<Study> {

    // (기존 검색 메서드 유지)
    Page<Study> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    // ⭐️ [수정됨] API 3-3: 내 스터디 목록 조회
    // 1. status 타입을 String -> Study.StudyStatus (Enum)으로 변경
    // 2. (:status IS NULL OR ...) 조건을 추가하여 전체 조회 기능 지원
    @Query("SELECT s FROM Study s JOIN s.studyMembers sm " +
            "WHERE sm.user.id = :userId " +
            "AND (:status IS NULL OR s.status = :status)")
    Page<Study> findStudiesByUserIdAndStatus(@Param("userId") Integer userId,
                                             @Param("status") Study.StudyStatus status,
                                             Pageable pageable);
}