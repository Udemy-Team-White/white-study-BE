package teamprojects.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateResponse {
    private String username; // 변경된 닉네임 (비밀번호 변경 시에는 null 또는 기존 닉네임)
}
