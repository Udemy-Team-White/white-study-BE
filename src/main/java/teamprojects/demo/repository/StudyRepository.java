package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;

public interface StudyRepository extends JpaRepository<Study, Integer> {

    // 나중에 API 4-3 "내 스터디 목록" 조회 시 Querydsl 또는 @Query와 함께 복잡한 조인 쿼리로 확장될 수 있어요.

}