package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfReportRewardResponse {

    private Integer selfReportId;
    private RewardDto reward; // 보상 정보 (null이면 보상 없음)

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardDto {
        private String type; // RELIABILITY
        private Integer changeAmount; // 1
        private String reason; // "셀프 보고서 150자 이상 작성"
    }
}