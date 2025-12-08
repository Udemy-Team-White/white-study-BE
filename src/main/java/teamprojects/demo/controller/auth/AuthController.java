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
import teamprojects.demo.dto.auth.AuthCheckEmailRequest;
import jakarta.validation.Valid;
import teamprojects.demo.dto.auth.*;
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

        // UserService의 signUp 메서드 호출하여 로직 처리
        User newUser = userService.signUp(request);

        // 응답 DTO (AuthRegisterResponse) 생성
        AuthRegisterResponse responseDto = AuthRegisterResponse.builder()
                .userId(newUser.getId())
                .username(newUser.getUsername())
                .build();

        // ApiResponse.onCreated()를 사용하여 201 응답 반환
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

        // UserService의 login 메서드 호출 (인증, 토큰 생성, 프로필 조회 로직 포함)
        AuthLoginResponse responseDto = userService.login(request);

        // ApiResponse.onSuccess()를 사용하여 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto);
    }
    /**
     * API 1-3: 이메일 중복 확인 요청
     * [POST] /api/auth/check-email
     * Body: { "email": "test@example.com" }
     */
    @PostMapping("/check-email")
    public ApiResponse<AuthCheckEmailResponse> checkEmailAvailability(
            @Valid @RequestBody AuthCheckEmailRequest request) {

        // request.getEmail()로 값을 꺼내서 서비스에 넘김
        AuthCheckEmailResponse responseDto = userService.checkEmailAvailability(request.getEmail());

        if (!responseDto.isAvailable()) {
            throw new CustomException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        return ApiResponse.onSuccess(responseDto);
    }
    /**
     * API 1-4: 닉네임 중복 확인
     * [POST] /api/auth/check-username
     * Body: { "username": "멋쟁이토마토" }
     */
    @PostMapping("/check-username")
    public ApiResponse<AuthCheckEmailResponse> checkUsernameAvailability(
            @Valid @RequestBody AuthCheckUsernameRequest request) {

        // 서비스 호출 (request.getUsername()으로 꺼내기)
        AuthCheckEmailResponse responseDto = userService.checkUsernameAvailability(request.getUsername());

        // 응답 분기 처리
        if (!responseDto.isAvailable()) {
            throw new CustomException(ErrorStatus.USERNAME_ALREADY_EXISTS);
        }

        return ApiResponse.onSuccess(responseDto);
    }
}