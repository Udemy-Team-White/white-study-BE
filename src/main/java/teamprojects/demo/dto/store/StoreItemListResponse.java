package teamprojects.demo.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemListResponse {

    private Integer myPoints; // 내 보유 포인트
    private List<StoreItemDto> items;
    private PageInfoDto pageInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreItemDto {
        private Integer itemId;
        private String itemName;
        private String description;
        private Integer price;
        private String itemType;
        private String imageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfoDto {
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
    }
}