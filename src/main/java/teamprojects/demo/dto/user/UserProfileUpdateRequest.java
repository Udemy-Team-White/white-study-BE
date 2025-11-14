// teamprojects.demo.dto.user.UserProfileUpdateRequest.java

package teamprojects.demo.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileUpdateRequest {

    // 새 닉네임: Optional이지만, 입력될 경우 길이를 검사합니다.
    @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
    private String username;

    // 현재 비밀번호: 비밀번호 변경 시 필수
    private String currentPassword;

    // 새 비밀번호: 비밀번호 변경 시 필수이며, 입력될 경우 길이를 검사합니다.
    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상, 20자 이하로 입력해야 합니다.")
    private String newPassword;
}