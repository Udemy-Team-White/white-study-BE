package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoDTO {

    private Integer page;
    private Integer size;
    private Integer totalPages;
    private Long totalElements; // 총 요소 수는 Long 타입이 일반적
}
