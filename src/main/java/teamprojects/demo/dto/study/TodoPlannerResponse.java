package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoPlannerResponse {

    // ⭐️ 응답의 최상위 List 요소입니다.
    // data: [ { TodoListDto }, { TodoListDto }, ... ]

    // 플래너 그룹 ID
    private Integer todoListId;

    // 플래너 그룹 제목 (예: '오전 루틴')
    private String title;

    // 해당 그룹의 할 일 목록
    private List<TodoItemDto> items;

    // --- TodoItemDto는 아래에서 정의합니다. ---

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodoItemDto {
        // 할 일 ID
        private Integer todoItemId;
        // 할 일 내용
        private String content;
        // 완료 여부
        private Boolean isCompleted;
    }
}