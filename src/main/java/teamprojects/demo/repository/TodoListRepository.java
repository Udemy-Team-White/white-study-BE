package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.TodoList;
import teamprojects.demo.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoListRepository extends JpaRepository<TodoList, Long> {

    // (API 5-2: 특정 날짜의 TODO 목록 조회 시 사용)
    // Service단에서 targetDate의 시작(00:00:00)과 끝(23:59:59) 시간을
    // startOfDay와 endOfDay로 만들어 호출합니다.
    List<TodoList> findByUserAndStudyAndTargetDateBetween(User user, Study study, LocalDateTime startOfDay, LocalDateTime endOfDay);
}