package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.StudyApplication;
import teamprojects.demo.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Integer> {

    // ⭐️ 1. 이 메서드를 추가해야 UserService.login이 정상 작동합니다.
    // ⭐️ (로그인 시, 스터디장(Leader)에게 온 'PENDING' 상태의 신청 건수를 조회)
    @Query("SELECT COUNT(sa) FROM StudyApplication sa JOIN sa.study s WHERE s.leader.id = :leaderId AND sa.status = 'PENDING'")
    Integer countPendingApplicationsByLeaderId(@Param("leaderId") Integer leaderId);

    // API 2-2 User, Study, Status(String)로 신청 내역 존재 여부 확인
    // (주의: Status가 Enum이 아니라 String으로 설계되었다고 가정했습니다. Enum이면 타입 변경 필요)
    boolean existsByUserAndStudyAndStatus(User user, Study study, String status);

    // (API 3-1: 신청자 목록 조회 시 사용)
    List<StudyApplication> findByStudyAndStatus(Study study, String status);

    // (API 2-3: 중복 신청 방지 시 사용)
    boolean existsByUserAndStudy(User user, Study study);

    // (API 3-2, 3-3: 승인/거절 시 특정 신청서 조회)
    Optional<StudyApplication> findByIdAndStudy(Long applicationId, Study study);
}