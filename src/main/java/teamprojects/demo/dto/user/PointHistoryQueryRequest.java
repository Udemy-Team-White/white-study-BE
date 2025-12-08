package teamprojects.demo.dto.user;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryQueryRequest {

    // page: integer (필수, 0부터 시작)
    @NotNull(message = "페이지 번호는 필수 입력 항목입니다.")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page;

    // size: integer (필수, 페이지당 개수)
    @NotNull(message = "페이지당 항목 수는 필수 입력 항목입니다.")
    @Min(value = 1, message = "페이지당 항목 수는 1 이상이어야 합니다.")
    private Integer size;
}