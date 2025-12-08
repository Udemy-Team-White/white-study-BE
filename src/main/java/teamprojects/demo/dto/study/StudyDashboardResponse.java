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
public class StudyDashboardResponse {

    private StudyInfoDTO studyInfo;
    private String nextScheduleDateTime; // 팀 결정 반영: 단일 DATETIME 필드
    private TodayProgressDTO myTodayProgress;
    private List<ReportSummaryDTO> recentReports;
    private List<MemberSummaryDTO> members;
}