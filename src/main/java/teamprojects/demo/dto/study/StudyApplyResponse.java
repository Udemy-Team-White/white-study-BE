package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyApplyResponse {

    // 생성된 신청서의 고유 ID
    private Integer applicationId;

    // 신청 상태 (항상 'PENDING'으로 반환됨)
    private String status;
}
