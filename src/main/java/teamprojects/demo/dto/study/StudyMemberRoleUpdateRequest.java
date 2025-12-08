package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyMemberRoleUpdateRequest {

    @NotBlank(message = "변경할 역할은 필수입니다.")
    private String role; // 예: "CO_LEADER", "MEMBER"
}