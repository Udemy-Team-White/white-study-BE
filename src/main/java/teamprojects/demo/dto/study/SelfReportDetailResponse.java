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

    // (참고: 제목, 요약 등도 필요하다면 여기에 추가하시면 됩니다.)
    private String subject;
    private String summary;
}