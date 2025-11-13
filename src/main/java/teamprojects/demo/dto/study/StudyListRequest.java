package teamprojects.demo.dto.study;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyListRequest {

    @NotNull(message = "페이지 번호는 필수입니다.")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page; // page: integer (필수, 0부터 시작)

    @NotNull(message = "페이지 크기는 필수입니다.")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    private Integer size; // size: integer (필수, 페이지당 개수)

    private String sortBy;    // sortBy: string (Optional, 예: 'latest', 'members')
    private String keyword;   // keyword: string (Optional, 검색어)
    private String category;  // category: string (Optional, 카테고리 필터링)
}