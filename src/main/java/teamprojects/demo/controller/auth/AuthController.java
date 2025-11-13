package teamprojects.demo.controller.auth;

import teamprojects.demo.dto.auth.AuthLoginRequest;
import teamprojects.demo.dto.auth.AuthLoginResponse;
import teamprojects.demo.dto.auth.AuthRegisterRequest;
import teamprojects.demo.dto.auth.AuthRegisterResponse;
import teamprojects.demo.dto.auth.AuthCheckEmailResponse;
import teamprojects.demo.entity.User;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.user.UserService;
import teamprojects.demo.global.common.exception.CustomException;
import teamprojects.demo.global.common.code.status.ErrorStatus;

import jakarta.validation.Valid; // @Valid 어노테이션
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


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
    /**
     * API 1-3: 이메일 중복 확인 요청
     * @param email (Query Parameter로 전송)
     * @return 200 OK (isAvailable: true/false) 또는 409 Conflict (isAvailable: false일 때)
     */
    @PostMapping("/check-email") // POST /api/auth/check-email?email=...
    public ApiResponse<AuthCheckEmailResponse> checkEmailAvailability(@RequestParam @NotBlank @Email String email) {

        // 1. UserService의 중복 확인 로직 호출 (사용 가능 여부 반환)
        AuthCheckEmailResponse responseDto = userService.checkEmailAvailability(email);

        // 2. 응답 분기 처리 (409 Conflict 처리)
        if (!responseDto.isAvailable()) {
            // isAvailable이 false (사용 중)일 경우, 409 Conflict를 던집니다.
            // GlobalExceptionHandler가 이를 가로채서 409 응답 JSON으로 변환합니다.
            throw new CustomException(ErrorStatus.EMAIL_ALREADY_EXISTS);
            // ⭐️ (참고: API 1-1의 EMAIL_ALREADY_EXISTS를 재사용합니다.)
        }

        // 3. isAvailable이 true (사용 가능)일 경우, 200 OK 응답을 반환합니다.
        return ApiResponse.onSuccess(responseDto);
    }
}