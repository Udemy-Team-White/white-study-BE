package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfReportDeleteResponse {
    private Integer deletedReportId;
}