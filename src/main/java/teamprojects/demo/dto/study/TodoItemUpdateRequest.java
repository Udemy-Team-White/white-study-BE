package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemUpdateRequest {

    @NotNull(message = "완료 상태(isCompleted)는 필수 입력 값입니다.")
    private Boolean isCompleted; // 변경할 최종 상태 (true 또는 false)
}
