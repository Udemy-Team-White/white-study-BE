package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.ReliabilityHistory;
import teamprojects.demo.entity.User;

// (참고: 만약 ReliabilityHistory 엔티티의 ID가 Long이라면 <ReliabilityHistory, Long> 으로 바꿔야 합니다.)
public interface ReliabilityHistoryRepository extends JpaRepository<ReliabilityHistory, Integer> {

    // API 3-5: 신뢰도 내역 조회
    Page<ReliabilityHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}