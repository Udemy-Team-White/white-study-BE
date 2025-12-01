package teamprojects.demo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserNicknameUpdateRequest {

    @NotBlank(message = "변경할 닉네임은 필수입니다.")
    @Size(max = 10, message = "닉네임은 10자 이내여야 합니다.")
    private String username; // ⭐️ 필드명이 'username'이어야 getUsername()이 생깁니다.
}