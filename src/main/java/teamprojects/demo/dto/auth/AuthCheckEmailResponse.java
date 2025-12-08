package teamprojects.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCheckEmailResponse {

    // true: 사용 가능, false: 이미 사용 중
    private boolean isAvailable;
}