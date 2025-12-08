package teamprojects.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginResponse {

    // JWT 토큰
    private String accessToken;

    // 사용자 프로필 정보 (중첩 객체)
    private UserProfileDto userProfile;

    // 내부 클래스로 DTO를 정의하여 계층 구조 표현
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileDto {
        private String username;          // 닉네임
        private Integer points;           // 현재 보유 포인트
        private Integer reliabilityScore; // 신뢰도 점수
        private Integer studyRequestCount; // 나에게 온 스터디 참여 요청 개수
    }
}