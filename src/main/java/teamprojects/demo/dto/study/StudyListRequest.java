package teamprojects.demo.dto.study;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyListRequest {

    @NotNull(message = "페이지 번호는 필수입니다.")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page; // 필수

    @NotNull(message = "페이지 크기는 필수입니다.")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    private Integer size; // 필수

    private String sortBy;
    private String keyword;
    private String category;
}