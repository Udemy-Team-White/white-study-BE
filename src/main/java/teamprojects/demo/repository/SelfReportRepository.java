package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.SelfReport;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface SelfReportRepository extends JpaRepository<SelfReport, Integer> {

    // ⭐️ API 4-1: 대시보드에서 최근 보고서 3개를 조회할 때 사용
    List<SelfReport> findTop3ByStudyOrderByCreatedAtDesc(Study study);

    // (API 5-9: 스터디의 보고서 목록 '페이지네이션' 조회 시 사용)
    Page<SelfReport> findByStudyOrderByCreatedAtDesc(Study study, Pageable pageable);

    // (API 5-8: 하루에 한 번만 작성 가능하도록 중복 체크 시 사용)
    boolean existsByUserAndStudyAndCreatedAtBetween(User user, Study study, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // (API 5-10: 상세 조회 시 '내가 쓴 글'인지 확인하는 로직에서 사용)
    Optional<SelfReport> findByIdAndUser(Long reportId, User user);

}