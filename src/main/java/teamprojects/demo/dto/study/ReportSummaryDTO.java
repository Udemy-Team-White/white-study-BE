package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDTO {

    private Integer reportId;
    private String authorUsername; // 작성자 닉네임
    private String createdAt;      // 작성일 (DATETIME)
}
