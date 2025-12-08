package teamprojects.demo.global.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;

public class SecurityUtils {

    public static Optional<Integer> getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // JwtFilter에서 Integer로 넣었으니, Integer로 받아야 함
        if (principal instanceof Integer) {
            return Optional.of((Integer) principal);
        }

        // UserDetails일 경우
        if (principal instanceof UserDetails) {
            String userIdString = ((UserDetails) principal).getUsername();
            try {
                return Optional.of(Integer.parseInt(userIdString));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        // String일 경우
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