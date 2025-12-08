package teamprojects.demo.dto.study;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyApplicantRejectRequest {
    // 거절 사유
    private String message;
}