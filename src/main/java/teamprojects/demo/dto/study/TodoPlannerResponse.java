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

    private Integer todoListId;
    private String title;
    private String cycleStartDate; // 계산된 사이클 시작일 (예: 이번 주 월요일)
    private String targetDate;     // 실제 DB에 저장된 목표 날짜
    private String createdDate;    // 생성일 (createdAt)
    // 해당 그룹의 할 일 목록
    private List<TodoItemDto> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodoItemDto {
        private Integer todoItemId;     // 할 일 ID
        private String content;         // 할 일 내용
        private Boolean isCompleted;    // 완료 여부
    }
}