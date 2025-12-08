package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyMemberKickResponse {
    private Integer kickedMemberId;
    private String message; // "정상적으로 처리되었습니다."
}
