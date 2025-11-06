// teamprojects.demo.dto.auth.AuthRegisterResponse.java

package teamprojects.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder // Service에서 쉽게 객체를 생성하기 위해 Builder 패턴 적용
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterResponse {

    // "userId": "integer (새로 생성된 USER의 고유 ID)"
    private Integer userId;

    // "username": "string (회원가입 시 사용한 닉네임)"
    private String username;
}