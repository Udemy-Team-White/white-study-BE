// teamprojects.demo.dto.auth.AuthRegisterRequest.java

package teamprojects.demo.dto.auth; // 패키지 변경: dto.auth

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
public class AuthRegisterRequest { // 클래스 이름 변경: AuthRegisterRequest

    // 1. 이메일 (email): 요청 명세 반영
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    // 2. 비밀번호 (password): 요청 명세 반영
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상, 20자 이하로 입력해야 합니다.")
    private String password;

    // 3. 닉네임 (username): 요청 명세 반영 및 ERD 길이 10 제한 적용
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 10, message = "닉네임은 10자 이하로 입력해야 합니다.")
    private String username;

    /**
     * DTO를 User Entity로 변환합니다. (Service에서 사용)
     * @param encodedPassword Bcrypt 암호화가 완료된 비밀번호
     * @param salt ERD에 따른 랜덤 Salt 값 (String)
     * @return User Entity
     */
    public User toEntity(String encodedPassword, String salt) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .username(this.username)
                .salt(salt) // 사용자님의 ERD 설계에 따른 salt 저장 (UUID 사용 예정)

                // 명세에 없는 약관 동의 관련 코드는 모두 삭제합니다.
                .role(UserRole.USER) // 기본 역할 설정
                .build();
    }
}