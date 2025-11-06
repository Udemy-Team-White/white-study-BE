package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.StudyApplication;
import teamprojects.demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Integer> {

    // (API 3-1: 신청자 목록 조회 시 사용)
    List<StudyApplication> findByStudyAndStatus(Study study, String status);

    // (API 2-3: 중복 신청 방지 시 사용)
    boolean existsByUserAndStudy(User user, Study study);

    // (API 3-2, 3-3: 승인/거절 시 특정 신청서 조회)
    Optional<StudyApplication> findByIdAndStudy(Long applicationId, Study study);
}