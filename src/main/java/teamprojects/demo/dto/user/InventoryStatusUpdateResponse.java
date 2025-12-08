package teamprojects.demo.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryStatusUpdateResponse {
    private Integer inventoryId;
    private Boolean isEquipped;
}
