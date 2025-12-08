package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemStatusResponse {
    private Integer todoItemId;
    private Boolean isCompleted;
}
