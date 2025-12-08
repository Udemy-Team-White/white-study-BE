package teamprojects.demo.dto.auth;

import teamprojects.demo.entity.User;
import teamprojects.demo.entity.UserRole; // UserRole Enum이 entity/enums 패키지에 있다고 가정
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {

    // 이메일 (email)
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    // 비밀번호 (password)
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상, 20자 이하로 입력해야 합니다.")
    private String password;

    // 닉네임 (username): ERD 길이 10 제한 적용
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
    private String username;

    /**
     * DTO를 User Entity로 변환합니다.
     * @param encodedPassword Bcrypt 암호화가 완료된 비밀번호
     * @param salt ERD에 따른 랜덤 Salt 값 (String)
     * @return User Entity
     */
    public User toEntity(String encodedPassword, String salt) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .username(this.username)
                .salt(salt)
                .role(UserRole.USER)
                .build();
    }
}