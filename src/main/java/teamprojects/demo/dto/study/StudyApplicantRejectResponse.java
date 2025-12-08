package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyApplicantRejectResponse {
    private Integer applicationId;
    private String status; // REJECTED
}
