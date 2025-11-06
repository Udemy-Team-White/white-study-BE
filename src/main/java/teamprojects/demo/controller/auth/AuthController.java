package teamprojects.demo.controller.auth;

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
}