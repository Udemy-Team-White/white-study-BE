// teamprojects.demo.repository.TodoListRepository.java (추가)
package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.TodoList;
import teamprojects.demo.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoListRepository extends JpaRepository<TodoList, Integer> {

    // (API 5-2: 특정 날짜의 TODO 목록 조회 시 사용)
    List<TodoList> findByUserAndStudyAndTargetDateBetween(User user, Study study, LocalDateTime startOfDay, LocalDateTime endOfDay);

    // api 5-8
    List<TodoList> findByStudy(Study study);
}
