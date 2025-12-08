package teamprojects.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReliabilityHistorySummaryDTO {

    private Integer historyId;
    private Integer changeAmount; // 변동량
    private String reason;       // 변동 사유
    private String createdAt;    // 변동 일시 (DATETIME)
}
