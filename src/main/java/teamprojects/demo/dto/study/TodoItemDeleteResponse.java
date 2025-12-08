package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoItemDeleteResponse {
    private Integer deletedTodoItemId;
}
