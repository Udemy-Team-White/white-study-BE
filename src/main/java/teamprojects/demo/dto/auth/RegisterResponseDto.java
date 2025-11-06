package teamprojects.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import teamprojects.demo.entity.User;
import teamprojects.demo.entity.UserProfile;

@Getter
@AllArgsConstructor // (Service에서 DTO를 쉽게 생성하기 위해)
public class RegisterResponseDto {

    private Long userId;
    private String username;

    // (Service에서 User와 UserProfile을 받아 DTO를 생성할 때 사용할 정적 팩토리 메서드)
    public static RegisterResponseDto of(User user, UserProfile userProfile) {
        return new RegisterResponseDto(user.getId(), userProfile.getUsername());
    }
}