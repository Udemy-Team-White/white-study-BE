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
public class SelfReportListResponse {

    private List<SelfReportSummaryDto> reports;
    private PageInfoDto pageInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelfReportSummaryDto {
        private Integer reportId;
        private String authorUsername;
        private String contentSnippet; // 내용 100자 요약
        private String createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfoDto {
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
    }
}