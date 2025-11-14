// teamprojects.demo.dto.study.StudySummaryDTO.java

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
    private String studyType;
    private List<String> categories; // N:M 관계의 카테고리 목록
    private Integer currentMembers;
    private Integer maxMembers;
    private String myRole; // LEADER 또는 MEMBER
}