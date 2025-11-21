package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.PointHistory;
import teamprojects.demo.entity.User;

// (참고: 만약 PointHistory 엔티티의 ID가 Long이라면 <PointHistory, Long> 으로 바꿔야 합니다.)
public interface PointHistoryRepository extends JpaRepository<PointHistory, Integer> {

    // API 3-4: 포인트 내역 조회
    Page<PointHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}