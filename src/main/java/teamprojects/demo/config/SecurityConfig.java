package teamprojects.demo.config; // 님의 패키지 경로

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // "이 파일은 Spring의 '설정' 파일입니다"
@EnableWebSecurity // "Spring Security를 '활성화'합니다"
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF/FormLogin/HttpBasic 비활성화 (기존 코드 유지)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 2. 세션(Session)을 사용하지 않음 (기존 코드 유지)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. ⭐️⭐️⭐️ API 및 Swagger 경로 접근 허용 (이 부분 수정) ⭐️⭐️⭐️
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/**",              // 1. 님이 만들 API
                                "/swagger-ui/**",       // 2. Swagger UI 페이지
                                "/v3/api-docs/**",      // 3. 403 에러가 났던 Swagger 데이터
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // 3-3. (기존 코드 유지) 그 외 모든 요청은 일단 인증 필요
                        .anyRequest().authenticated()
                );

        // (나중에 [Phase 2]에서 JWT 필터를 여기에 추가하게 됩니다)

        return http.build();
    }
    // ⭐️ 3. CORS 설정을 위한 Bean을 SecurityConfig 클래스 "내부"에 추가합니다.
    // (passwordEncoder Bean과 같은 레벨)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ⭐️ (개발 중) 모든 출처(Origin)를 허용합니다.
        configuration.setAllowedOrigins(Arrays.asList("*"));
        // ⭐️ 모든 HTTP 메서드 (GET, POST, PUT, DELETE 등)를 허용합니다.
        configuration.setAllowedMethods(Arrays.asList("*"));
        // ⭐️ 모든 헤더를 허용합니다.
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // (필요 시 주석 해제)
        // configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // ⭐️ "/**" (모든 경로)에 대해 위 설정을 적용합니다.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // 비밀번호 암호화도구를 Bean으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Spring Security가 제공하는 표준 Bcrypt 암호화 도구를 반환합니다.
        return new BCryptPasswordEncoder();
    }
}