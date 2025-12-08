package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.TodoList;
import teamprojects.demo.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoListRepository extends JpaRepository<TodoList, Integer> {

    // API 5-2: 특정 날짜의 TODO 목록 조회 시 사용
    Optional<TodoList> findFirstByUserAndStudyAndTargetDateBetween(User user, Study study, LocalDateTime start, LocalDateTime end);

    // api 5-8
    List<TodoList> findByStudy(Study study);

    //이 스터디에서 내가 만든 '가장 최근(날짜가 제일 늦은)' TodoList 하나 조회
    Optional<TodoList> findTop1ByUserAndStudyOrderByTargetDateDesc(User user, Study study);


}
