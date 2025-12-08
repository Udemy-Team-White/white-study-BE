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
public class StudyApplicantResponse {

    private Integer applicationId; // 신청서 ID
    private String message;        // 신청 메시지
    private String appliedAt;      // 신청 일시

    // 유저 정보를 별도 객체로 구조화
    private ApplicantDto user;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicantDto {
        private Integer userId;
        private String username;
        private String profileImageUrl;
        private Integer reliabilityScore;

        // 착용 중인 아이템 이미지/이름 목록
        private List<String> equippedItems;
    }
}
