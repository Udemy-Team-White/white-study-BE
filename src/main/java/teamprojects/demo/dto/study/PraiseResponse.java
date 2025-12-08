package teamprojects.demo.dto.study;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PraiseResponse {
    private Integer praiseId;
    private RewardDto reward;
    private Integer myRemainingPoints;

    @Getter
    @Builder
    public static class RewardDto {
        private String type; // "POINT"
        private Integer changeAmount;
        private String reason;
    }
}
