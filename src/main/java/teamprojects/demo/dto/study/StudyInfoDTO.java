package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyInfoDTO {

    private Integer studyId;
    private String studyName;
    private String title;
    private String status;
    private String startDate;
    private String endDate;
    private String myRole;
}
