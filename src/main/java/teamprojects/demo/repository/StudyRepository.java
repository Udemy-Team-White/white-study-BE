package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamprojects.demo.entity.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface StudyRepository extends JpaRepository<Study, Integer>, JpaSpecificationExecutor<Study> {

    Page<Study> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    List<Study> findByEndDateBeforeAndStatusNot(LocalDate date, Study.StudyStatus status);

    // API 3-3: 내 스터디 목록 조회
    @Query("SELECT s FROM Study s JOIN s.studyMembers sm " +
            "WHERE sm.user.id = :userId " +
            "AND (:status IS NULL OR s.status = :status)")
    Page<Study> findStudiesByUserIdAndStatus(@Param("userId") Integer userId,
                                             @Param("status") Study.StudyStatus status,
                                             Pageable pageable);
}