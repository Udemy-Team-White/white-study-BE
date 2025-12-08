package teamprojects.demo.dto.study;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemCreateRequest {

    @NotBlank(message = "할 일 내용은 필수입니다.")
    private String content; // 할 일 내용

    // content 필드에 데이터가 들어올 때 이 메서드가 실행됩니다.
    @JsonSetter("content")
    public void setContent(Object value) {
        if (value instanceof String) {
            this.content = (String) value; // 문자열이면 그대로 저장
        } else if (value != null) {
            this.content = value.toString();
        }
    }
}
