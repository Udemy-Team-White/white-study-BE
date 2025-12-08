package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryDTO {

    private Integer userId;
    private String username;
    private String role; // 'LEADER', 'MEMBER'
    private String imgUrl;
    private List<String> equippedItems;
}