package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfReportUpdateRequest {

    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content;
    private String subject;
    private String summary;
}
