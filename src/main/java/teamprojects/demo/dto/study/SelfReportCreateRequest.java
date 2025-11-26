package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SelfReportCreateRequest {

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String subject;

    @NotBlank(message = "요약은 필수 입력 항목입니다.")
    private String summary;

    @NotBlank(message = "본문 내용은 필수 입력 항목입니다.")
    private String content;
}
