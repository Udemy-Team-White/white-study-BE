package teamprojects.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    // 보안을 위해 최소/최대 길이 패턴(@Pattern) 등을 추가해도 됩니다.
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 10, message = "닉네임은 10자를 초과할 수 없습니다.")
    private String username;

    // salt는 클라이언트(React)가 보내는 것이 아니고 나중에 만들 AuthService에서 보안을 위해 "직접 생성"할 것이므로 요청 DTO에는 포함시키지 않았습니다.)
}