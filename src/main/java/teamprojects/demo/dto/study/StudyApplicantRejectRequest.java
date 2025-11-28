package teamprojects.demo.dto.study;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyApplicantRejectRequest {
    // 거절 사유
    // DB에 저장할 곳이 없다면 로직에서 무시하거나, 별도 컬럼이 필요
    private String message;
}