package teamprojects.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthCheckUsernameRequest {

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String username;
}
