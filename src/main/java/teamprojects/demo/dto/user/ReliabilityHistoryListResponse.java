package teamprojects.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import teamprojects.demo.dto.study.PageInfoDTO;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReliabilityHistoryListResponse {

    private Integer currentScore; // 현재 총 신뢰도 점수
    private List<ReliabilityHistorySummaryDTO> history; // 신뢰도 변동 내역 목록
    private PageInfoDTO pageInfo; // 페이지 정보
}
