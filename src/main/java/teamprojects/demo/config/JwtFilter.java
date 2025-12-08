package teamprojects.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import teamprojects.demo.entity.User;
import teamprojects.demo.repository.UserRepository;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;

@RequiredArgsConstructor //  UserRepository 주입을 위해 추가
public class JwtFilter extends OncePerRequestFilter {

    private final UserRepository userRepository; // DB 조회를 위해 필요

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                //  "Bearer " 떼어내기
                String token = authHeader.substring(7);

                //  토큰 해독 (Base64 디코딩)
                // (아까 UserService에서 암호화한 걸 다시 풉니다)
                byte[] decodedBytes = Base64.getDecoder().decode(token);
                String decodedString = new String(decodedBytes); // 결과예시: "1:test@email.com"

                //  User ID 추출 (콜론 : 기준으로 앞부분)
                String[] parts = decodedString.split(":");
                Integer userId = Integer.parseInt(parts[0]);

                //  DB에서 유저 조회
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    // 인증 객체 생성 & 등록
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user.getId(), null, Collections.emptyList());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // 토큰 형식이 이상하거나 유저가 없으면 그냥 넘어감 (인증 실패 처리됨)
                System.out.println("인증 실패: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}