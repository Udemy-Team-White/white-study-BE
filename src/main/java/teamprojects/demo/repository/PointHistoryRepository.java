package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.PointHistory;
import teamprojects.demo.entity.User;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // (API 4-4: '내 포인트 내역 조회' 페이징 처리 시 사용)
    Page<PointHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}