package teamprojects.demo.config; // 님의 패키지 경로

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

    // (API 테스트를 위해)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF/FormLogin/HttpBasic 비활성화 (REST API 서버이므로)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 2. 세션(Session)을 사용하지 않음 (STATELESS)
                // (나중에 JWT(토큰)를 사용할 것이기 때문)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 임시로 모든 API 접근 허용
                // [Phase 1] 개발 및 테스트를 위해, /api/로 시작하는 모든 요청을
                // 전부 통과시킵니다.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // "/api/" 하위 모든 경로
                        .anyRequest().authenticated() // (그 외 모든 요청은 일단 막음 - 지금은 큰 의미 없음)
                );

        // (나중에 [Phase 2]에서 JWT 필터를 여기에 추가하게 됩니다)

        return http.build();
    }

    // 비밀번호 암호화도구를 Bean으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Spring Security가 제공하는 표준 Bcrypt 암호화 도구를 반환합니다.
        return new BCryptPasswordEncoder();
    }

    // --- (salt 컬럼 관련) ---
    // 위 @Bean의 'BcryptPasswordEncoder'는 님이 'User' 엔티티에 만드신
    // 'salt' 컬럼을 사용하지 "않습니다."
    //
    // 만약 님이 'salt' 컬럼을 "반드시" 사용해야 한다면,
    // [Phase 2] 회원가입 로직을 짤 때,
    // 'return new BcryptPasswordEncoder();' 이 부분을
    // 님이 'salt'를 직접 처리하는 'CustomPasswordEncoder' 구현체로
    // 교체하는 코드를 "직접" 작성하셔야 합니다!
    //
    // (만약 'salt' 컬럼을 지우기로 하셨다면, 이 Bean을 그대로 쓰시면 됩니다.)
    // ---
}

//package teamprojects.demo.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BcryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/**").permitAll()
//                        .anyRequest().authenticated()
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BcryptPasswordEncoder();
//    }
//}