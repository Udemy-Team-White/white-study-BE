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
public class StudyDetailResponse {

    // 스터디 기본 상세 정보
    private StudyInfoDto studyInfo;

    //카테고리 목록
    private List<CategoryDto> categories;

    // 스터디장 정보
    private StudyLeaderDto studyLeader;

    // 현재 접속자 상태 (GUEST, LEADER, MEMBER, APPLIED, NONE)
    private String userStatus;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyInfoDto {
        private Integer studyId;
        private String title;
        private String content;
        private String studyType;
        private String status;
        private Integer currentMembers;
        private Integer maxMembers;
        private String closedAt;
        private String startDate;
        private String endDate;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Integer categoryId;
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyLeaderDto {
        private String username;
        private Integer reliabilityScore;
    }
}