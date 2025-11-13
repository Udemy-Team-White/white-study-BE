package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// ⭐️ API 3-3 응답을 위한 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoListCreateResponse {

    // 새로 생성된 그룹 ID
    private Integer todoListId;

    // 생성된 그룹 제목
    private String title;

    // 그룹의 기준 날짜 (Request와 동일하게 LocalDateTime 사용)
    private LocalDateTime targetDate;

    // 생성 시에는 항상 빈 목록을 반환합니다.
    // API 3-2에서 정의한 TodoItemDto의 구조를 재활용하여 빈 배열을 명시합니다.
    private List<TodoPlannerResponse.TodoItemDto> items;
}