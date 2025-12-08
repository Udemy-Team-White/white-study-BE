package teamprojects.demo.dto.store;

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
public class StoreItemListRequest {

    private Integer page;
    private Integer size;

    private String filter; // 아이템 타입 필터 (BACKGROUND, BADGE 등)

    // 기본값 처리
    public Integer getPage() {
        return page == null ? 0 : page;
    }

    public Integer getSize() {
        return size == null ? 12 : size;
    }
}
