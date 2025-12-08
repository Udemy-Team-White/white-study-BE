package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfReportDetailResponse {

    private Integer reportId;
    private String authorUsername;
    private String content;
    private String createdAt;
    private Boolean isMine; // 내가 쓴 글인지 여부
    private String subject;
    private String summary;
}