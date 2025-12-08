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
public class PointHistoryListResponse {

    private Integer currentPoints; // 현재 총 보유 포인트
    private List<PointHistorySummaryDTO> history; // 포인트 변동 내역 목록
    private PageInfoDTO pageInfo; // 페이지 정보 (재사용)
}