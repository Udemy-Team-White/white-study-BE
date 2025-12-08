package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyApplicantApproveResponse {
    private Integer memberId; // 새로 생긴 멤버 ID
    private Integer userId;
    private String username;
    private String role;      // MEMBER
}
