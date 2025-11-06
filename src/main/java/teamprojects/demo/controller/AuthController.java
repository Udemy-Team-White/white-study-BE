package teamprojects.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamprojects.demo.dto.auth.RegisterRequestDto;
import teamprojects.demo.dto.auth.RegisterResponseDto;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDto>> register(
            @Valid @RequestBody RegisterRequestDto requestDto
    ) {
        RegisterResponseDto responseDto = authService.register(requestDto);
        return ResponseEntity.status(201).body(ApiResponse.onCreated(responseDto));
    }

    // API 1-2: 로그인은 나중에 여기에 @PostMapping("/login")으로 추가
}