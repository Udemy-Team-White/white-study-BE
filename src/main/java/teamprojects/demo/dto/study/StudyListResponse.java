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
public class StudyListResponse {

    // "studies" 필드: 목록
    private List<StudyDto> studies;

    // "pageInfo" 필드: 페이지 정보
    private PageInfoDto pageInfo;

    // StudyListResponse.java 내부에 중첩 클래스로 정의합니다.
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudyDto {
        private Integer studyId;        // studyId
        private String title;
        private String studyName;
        private String studyType;       // studyType
        private List<String> categories; // categories (N:M 관계로 인해 String List로 받음)
        private Integer currentMembers; // currentMembers (STUDY_MEMBER COUNT)
        private Integer maxMembers;     // maxMembers
        private String closedAt;        // closedAt (Datetime)
        private String status;          // status (모집 상태)
        private String createdAt;       // 작성일
    }
    // StudyListResponse.java 내부에 중첩 클래스로 정의합니다.
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfoDto {
        private Integer page;           // page (현재 페이지)
        private Integer size;           // size (페이지당 항목 수)
        private Integer totalPages;     // totalPages (총 페이지 수)
        private Long totalElements;     // totalElements (전체 스터디 개수)
    }
}