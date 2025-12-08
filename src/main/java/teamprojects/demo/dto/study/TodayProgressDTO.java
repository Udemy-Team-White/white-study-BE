package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayProgressDTO {

    private Integer totalItems;           // 오늘 날짜의 총 TODO 아이템 개수
    private Integer completedItems;       // 그중 완료한 개수
    private Integer progressPercentage;   // 0 ~ 100
}