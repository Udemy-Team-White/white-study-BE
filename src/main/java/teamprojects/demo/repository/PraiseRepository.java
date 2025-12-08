package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Praise;
import teamprojects.demo.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public interface PraiseRepository extends JpaRepository<Praise, Integer> {

    // (API 4-1: 마이페이지 칭찬 횟수 요약 시 사용)
    long countByReceiver(User receiver);


    // (API 6-5: 칭찬 횟수 제한 로직 등에 사용될 수 있음)
    long countBySenderAndCreatedAtAfter(User sender, LocalDateTime after);

    List<Praise> findAllByReceiverId(Integer receiverId);
}