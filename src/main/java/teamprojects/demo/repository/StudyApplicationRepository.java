package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.StudyApplication;
import teamprojects.demo.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

// ⭐️ <StudyApplication, Integer> 로 선언되어 있음 (ID 타입 = Integer)
public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Integer> {

    // ⭐️ [완벽함] 로그인 시 알림 개수 조회 쿼리
    @Query("SELECT COUNT(sa) FROM StudyApplication sa JOIN sa.study s WHERE s.leader.id = :leaderId AND sa.status = 'PENDING'")
    Integer countPendingApplicationsByLeaderId(@Param("leaderId") Integer leaderId);

    // API 2-2 신청 내역 존재 여부
    boolean existsByUserAndStudyAndStatus(User user, Study study, String status);

    // API 3-1 신청자 목록 조회
    List<StudyApplication> findByStudyAndStatus(Study study, String status);

    // API 2-3 중복 신청 방지
    boolean existsByUserAndStudy(User user, Study study);

    // 🚨 [수정 완료] Long -> Integer로 변경해야 합니다!
    // (위에서 JpaRepository<..., Integer>로 선언했기 때문입니다.)
    Optional<StudyApplication> findByIdAndStudy(Integer applicationId, Study study);

    //유저와 스터디 정보로 신청 내역 리스트를 가져오는 메서드
    List<StudyApplication> findByUserAndStudy(User user, Study study);
}