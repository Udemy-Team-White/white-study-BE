package teamprojects.demo.dto.store;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemPurchaseResponse {
    private Integer inventoryId;
    private PurchasedItemDto purchasedItem;
    private Integer remainingPoints;

    @Getter
    @Builder
    public static class PurchasedItemDto {
        private Integer itemId;
        private String itemName;
    }
}