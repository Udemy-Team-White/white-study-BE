package teamprojects.demo.global.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;

public class SecurityUtils {

    /**
     * 현재 Security Context에 저장된 인증된 사용자의 ID(Integer)를 Optional 형태로 반환합니다.
     * ⭐️ 전제: Spring Security의 Principal(주체)에 DB의 user_id(Integer)가 String 형태로 저장되어 있다고 가정합니다.
     * @return 현재 인증된 사용자 ID (Optional<Integer>)
     */
    public static Optional<Integer> getCurrentUserId() {
        // 1. Security Context에서 Authentication 객체를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보가 없거나 인증되지 않은 경우 (비로그인 상태)
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        // 3. Principal 객체를 가져옵니다. (사용자 정보)
        Object principal = authentication.getPrincipal();

        // 4. Principal이 UserDetails(스프링 기본 사용자 객체) 형태일 경우
        if (principal instanceof UserDetails) {
            String userIdString = ((UserDetails) principal).getUsername();
            try {
                // UserDetails의 getUsername()에 DB의 user_id가 들어있다고 가정하고 Integer로 변환
                return Optional.of(Integer.parseInt(userIdString));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        // 5. Principal이 String 형태 (JWT에서 ID만 꺼내 넣었을 경우)일 경우
        if (principal instanceof String) {
            try {
                return Optional.of(Integer.parseInt((String) principal));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}