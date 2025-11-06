package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.ReliabilityHistory;
import teamprojects.demo.entity.User;

public interface ReliabilityHistoryRepository extends JpaRepository<ReliabilityHistory, Integer> {

    // (API 4-5: '내 신뢰도 내역 조회' 페이징 처리 시 사용)
    Page<ReliabilityHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}