// teamprojects.demo.dto.study.MemberSummaryDTO.java

package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSummaryDTO {

    private Integer userId;
    private String username;
    private String role; // 'LEADER', 'MEMBER'
}