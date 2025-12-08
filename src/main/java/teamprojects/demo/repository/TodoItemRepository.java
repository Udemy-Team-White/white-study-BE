package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import teamprojects.demo.entity.TodoItem;
import teamprojects.demo.entity.TodoList;
import java.time.LocalDateTime;
import java.util.List;

public interface TodoItemRepository extends JpaRepository<TodoItem, Integer> {

    // API 5-2: TodoList에 속한 모든 Item을 조회할 때 사용
    List<TodoItem> findByTodoListOrderByOrderIndexAsc(TodoList todoList);

    // API 4-1: 총 TodoItem 개수 조회 (TodoItem - TodoList 조인)
    @Query("SELECT COUNT(ti) FROM TodoItem ti JOIN ti.todoList tl WHERE tl.study.id = :studyId AND tl.user.id = :userId AND tl.targetDate BETWEEN :startOfDay AND :endOfDay")
    Long countTotalItemsForDashboard(
            @Param("studyId") Integer studyId,
            @Param("userId") Integer userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    //  API 4-1: 완료된 TodoItem 개수 조회 (isCompleted = TRUE 조건 추가)
    @Query("SELECT COUNT(ti) FROM TodoItem ti JOIN ti.todoList tl WHERE tl.study.id = :studyId AND tl.user.id = :userId AND ti.isCompleted = TRUE AND tl.targetDate BETWEEN :startOfDay AND :endOfDay")
    Long countCompletedItemsForDashboard(
            @Param("studyId") Integer studyId,
            @Param("userId") Integer userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}