package teamprojects.demo.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import teamprojects.demo.dto.user.MypageDataResponse;
import teamprojects.demo.global.common.ApiResponse; // ⭐️ 공통 응답 객체 (경로 확인)
import teamprojects.demo.service.user.UserService; // ⭐️ UserService 주입을 위함 (경로 확인)

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users") // ⭐️ User 도메인 공통 URL Prefix
public class UserController {

    // ⭐️ UserService 주입
    private final UserService userService;

    /**
     * API 3-1: 마이페이지 초기 데이터 조회
     * URL: GET /api/users/me/mypage-data
     * (로그인 필요)
     * @return 200 OK (MypageDataResponse)
     */
    @GetMapping("/me/mypage-data") // 최종 URL: /api/users/me/mypage-data
    public ApiResponse<MypageDataResponse> getUserMypageData() {

        // 1. UserService의 마이페이지 데이터 조회 메서드 호출
        MypageDataResponse responseDto = userService.getUserMypageData();

        // 2. 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto);
    }

    // 이 아래에 프로필 수정, 포인트 내역 조회 등 다른 User 관련 API를 추가하시면 됩니다.
}