package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelfReportDeleteResponse {
    private Integer deletedReportId;

    @Builder
    public SelfReportDeleteResponse(Integer deletedReportId) {
        this.deletedReportId = deletedReportId;
    }
}