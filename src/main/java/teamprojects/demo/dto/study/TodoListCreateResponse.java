package teamprojects.demo.dto.study;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoListCreateResponse {

    private Integer todoListId;

    private String title;

    // 응답 나갈 때도 포맷을 예쁘게 맞춰줍니다! (프론트 오류 방지용)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
    private LocalDateTime targetDate;

    private List<TodoPlannerResponse.TodoItemDto> items;
}