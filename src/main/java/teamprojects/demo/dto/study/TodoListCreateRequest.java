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
    private LocalDateTime targetDate;

    // 선택 필드: 플래너 그룹 제목 (예: "1주차 목표")
    @Size(max = 100, message = "플래너 제목은 100자 이내여야 합니다.")
    private String title;
}
