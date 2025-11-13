package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoListCreateRequest {

    // ⭐️ 필수 필드: 플래너가 될 기준 날짜 및 시간
    @NotNull(message = "대상 날짜(targetDate)는 필수 입력 값입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // 명세서 형식 지정
    private LocalDateTime targetDate;

    // 선택 필드: 플래너 그룹 제목
    @Size(max = 100, message = "플래너 제목은 100자 이내여야 합니다.")
    private String title;
}
