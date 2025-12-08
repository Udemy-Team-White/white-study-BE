package teamprojects.demo.dto.study;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyApplyRequest {

    // Optional 필드지만, 너무 긴 텍스트 방지를 위해 길이 제한을 둡니다.
    @Size(max = 255, message = "신청 메시지는 255자 이내로 작성해주세요.")
    private String message;
}