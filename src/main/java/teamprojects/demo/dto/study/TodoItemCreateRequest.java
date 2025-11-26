package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemCreateRequest {

    @NotBlank(message = "할 일 내용은 필수입니다.")
    private String content; // 할 일 내용
}
