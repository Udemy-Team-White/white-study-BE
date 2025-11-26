package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfReportUpdateRequest {

    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content;

    // (참고: 만약 제목/요약도 수정 가능하다면 여기에 필드 추가하면 됩니다)
    private String subject;
    private String summary;
}
