package teamprojects.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MypageDataResponse {

    // 프로필 기본 정보
    private ProfileInfoDto profileInfo;

    // 활동 요약 정보
    private ActivitySummaryDto activitySummary;

    // 보유 아이템 목록
    private List<InventorySummaryDto> inventorySummary;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileInfoDto {
        private String username;
        private String email;
        private String bio;     // UserProfile의 introduction 필드를 bio로 매핑 가정
        private String imgUrl;
        private List<String> equippedItems;// UserProfile의 profileImageUrl 필드를 imgUrl로 매핑 가정
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySummaryDto {
        private Integer points;
        private Integer reliabilityScore;
        private List<PraiseDto> praise; // 칭찬 목록 (중첩)
        private Integer inProgressStudies; // 현재 참여중인 스터디 수
        private Integer completedStudies;  // 완료한 스터디 수
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PraiseDto {
        private String message; // 칭찬 메시지 내용
        private Integer count;  // 해당 메시지를 받은 횟수
        private String praiseType;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventorySummaryDto {
        private Integer itemId;
        private String itemName;
        private boolean equipped;
        private String imageUrl;
    }
}