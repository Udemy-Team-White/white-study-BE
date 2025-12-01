package teamprojects.demo.dto.study;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoItemCreateRequest {

    @NotBlank(message = "할 일 내용은 필수입니다.")
    private String content; // 할 일 내용

    // ⭐ [마법의 코드] content 필드에 데이터가 들어올 때 이 메서드가 실행됩니다.
    @JsonSetter("content")
    public void setContent(Object value) {
        if (value instanceof String) {
            this.content = (String) value; // 문자열이면 그대로 저장
        } else if (value != null) {
            // 객체로 들어왔다면 (예: {value: "..."}), 그냥 통째로 문자열로 바꿔서 저장하거나
            // 혹은 "프론트 실수"라고 판단하고 그 안의 값을 꺼내려 시도할 수 있습니다.
            // 여기서는 간단하게 toString()으로 처리해서 에러만 막습니다.
            this.content = value.toString();
        }
    }
}
