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

    // 1. 스터디 기본 상세 정보
    private StudyInfoDto studyInfo;

    // 2. 카테고리 목록
    private List<CategoryDto> categories;

    // 3. 스터디장 정보
    private StudyLeaderDto studyLeader;

    // 4. 현재 접속자 상태 (GUEST, LEADER, MEMBER, APPLIED, NONE)
    private String userStatus;

    // --- 내부 클래스 정의 ---

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyInfoDto {
        private Integer studyId;
        private String title;
        private String content;
        private String studyType;      // ONLINE, OFFLINE, MIXED
        private String status;         // RECRUITING, etc.
        private Integer currentMembers;
        private Integer maxMembers;
        private String closedAt;       // 모집 마감일 (String)
        private String startDate;      // 스터디 시작일 (String)
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