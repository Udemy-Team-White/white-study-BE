package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySummaryDTO {

    private Integer studyId;
    private String title;
    private String studyName;
    private String studyType;
    private List<String> categories;
    private Integer currentMembers;
    private Integer maxMembers;
    private String myRole;
    private String status;
}