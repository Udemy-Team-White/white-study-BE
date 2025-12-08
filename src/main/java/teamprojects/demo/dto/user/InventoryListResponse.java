package teamprojects.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryListResponse {

    private List<InventoryItemDto> items;
    private PageInfoDto pageInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItemDto {
        private Integer inventoryId;
        private String itemName;
        private String itemType;
        private String acquiredAt;
        private Boolean isEquipped;
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