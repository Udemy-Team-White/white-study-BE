package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyMemberRoleUpdateResponse {
    private Integer memberId;
    private Integer userId;
    private String username;
    private String newRole;
}
