package teamprojects.demo.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryStatusUpdateRequest {
    @NotNull(message = "장착 여부는 필수입니다.")
    private Boolean isEquipped;
}