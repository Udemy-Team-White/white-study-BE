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
public class MyStudiesListResponse {

    // studies: List<StudySummaryDTO>
    private List<StudySummaryDTO> studies;

    // pageInfo: PageInfoDTO
    private PageInfoDTO pageInfo;
}