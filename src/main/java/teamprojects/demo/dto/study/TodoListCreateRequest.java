package teamprojects.demo.dto.study;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Setter 추가 (혹시 모를 파라미터 바인딩 대비)

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoListCreateRequest {

    // ⭐️ [수정] 백엔드에서 자동 계산하므로 @NotNull 제거! (보내도 되고 안 보내도 됨)
    // 만약 프론트가 특정 날짜를 강제하고 싶으면 보내고, 아니면 null로 보내면 됩니다.
    private LocalDateTime targetDate;

    // 선택 필드: 플래너 그룹 제목 (예: "1주차 목표")
    @Size(max = 100, message = "플래너 제목은 100자 이내여야 합니다.")
    private String title;
}
