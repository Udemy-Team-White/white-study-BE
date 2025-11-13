package teamprojects.demo.controller.auth;

import teamprojects.demo.dto.auth.AuthLoginRequest;
import teamprojects.demo.dto.auth.AuthLoginResponse;
import teamprojects.demo.dto.auth.AuthRegisterRequest;
import teamprojects.demo.dto.auth.AuthRegisterResponse;
import teamprojects.demo.entity.User;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.user.UserService;
import jakarta.validation.Valid; // @Valid 어노테이션
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth") // '/api/auth' 경로로 들어오는 요청을 처리
public class AuthController {

    private final UserService userService;

    /**
     * API 1-1: 회원가입 요청
     * @param request (email, password, username) - @Valid로 DTO 유효성 검사
     * @return ApiResponse<AuthRegisterResponse> (userId, username)
     */
    @PostMapping("/register") // POST /api/auth/register
    public ApiResponse<AuthRegisterResponse> register(@Valid @RequestBody AuthRegisterRequest request) {

        // 1. UserService의 signUp 메서드 호출하여 로직 처리
        User newUser = userService.signUp(request);

        // 2. 응답 DTO (AuthRegisterResponse) 생성
        AuthRegisterResponse responseDto = AuthRegisterResponse.builder()
                .userId(newUser.getId())
                .username(newUser.getUsername())
                .build();

        // 3. ApiResponse.onCreated()를 사용하여 201 응답 반환
        return ApiResponse.onCreated(responseDto);
    }
    /**
     * API 1-2: 로그인 인증 요청
     * * 응답:
     * - 200 OK (AuthLoginResponse 포함)
     * - 401 Unauthorized (UserService에서 처리)
     */
    @PostMapping("/login") // POST /api/auth/login
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {

        // 1. UserService의 login 메서드 호출 (인증, 토큰 생성, 프로필 조회 로직 포함)
        AuthLoginResponse responseDto = userService.login(request);

        // 2. ApiResponse.onSuccess()를 사용하여 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto);
    }
}