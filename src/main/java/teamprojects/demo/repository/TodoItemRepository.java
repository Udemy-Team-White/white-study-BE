package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.TodoItem;
import teamprojects.demo.entity.TodoList;

import java.util.List;

public interface TodoItemRepository extends JpaRepository<TodoItem, Integer> {

    // (API 5-2: TodoList에 속한 모든 Item을 조회할 때 사용)
    List<TodoItem> findByTodoListOrderByOrderIndexAsc(TodoList todoList);

    // (특별한 이름 규칙의 쿼리 메서드가 당장은 필요하지 않습니다.
    //  API 5-5, 5-6, 5-7(수정/삭제)은 Service단에서
    //  .findById()로 항목을 찾은 뒤, 엔티티를 변경하고 .save()하는
    //  방식(Dirty Checking)으로 처리하는 것이 일반적입니다.)
}