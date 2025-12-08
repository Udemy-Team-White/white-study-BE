// teamprojects.demo.repository.ScheduleRepository.java
package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Schedule;
import teamprojects.demo.entity.Study;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    // (API 5-1 대시보드 또는 '스터디 일정' 페이지에서 해당 스터디의 모든 등록된 일정을 조회할 때 사용)
    List<Schedule> findByStudy(Study study);

}