package teamprojects.demo.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamprojects.demo.dto.user.MypageDataResponse;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import teamprojects.demo.dto.user.UserProfileUpdateRequest;
import teamprojects.demo.dto.user.UserProfileUpdateResponse;
import teamprojects.demo.entity.User;
import teamprojects.demo.dto.study.MyStudiesListResponse;
import teamprojects.demo.dto.study.MyStudiesQueryRequest;
import org.springframework.web.bind.annotation.ModelAttribute;
import teamprojects.demo.dto.user.PointHistoryListResponse;
import teamprojects.demo.dto.user.PointHistoryQueryRequest;
import teamprojects.demo.dto.user.ReliabilityHistoryListResponse;
import teamprojects.demo.dto.user.ReliabilityHistoryQueryRequest;


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

    /**
     * API 3-2: 프로필 수정 요청 (닉네임, 비밀번호)
     * [PATCH] /api/users/me/profile
     * * @param request 수정할 정보 (username, currentPassword, newPassword)
     * @return 수정된 닉네임 정보
     */
    @PatchMapping("/me/profile")
    public ApiResponse<UserProfileUpdateResponse> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        // ⚠️ TODO: 현재는 로그인이 구현되지 않았으므로, 테스트를 위해 userId를 1로 고정합니다.
        // 추후 로그인(API 1-2) 및 JWT 구현 완료 시, @AuthenticationPrincipal 등을 통해
        // 실제 로그인한 사용자의 ID를 받아오도록 수정해야 합니다.
        Integer userId = 1;

        // 1. 서비스 로직 호출
        User updatedUser = userService.updateProfile(userId, request);

        // 2. 응답 DTO 생성
        UserProfileUpdateResponse response = UserProfileUpdateResponse.builder()
                .username(updatedUser.getUsername())
                .build();

        // 3. 성공 응답 반환 (200 OK)
        // (메시지는 ApiResponse.onSuccess()의 기본 메시지인 "요청에 성공했습니다."가 나갑니다.)
        return ApiResponse.onSuccess(response);
    }
    /**
     * API 3-3: 내 스터디 목록 조회
     * URL: GET /api/users/me/studies?status=...&page=...
     * @param query (status, page, size) - @ModelAttribute로 Query Parameter 바인딩
     * @return 200 OK (MyStudiesListResponse)
     */
    @GetMapping("/me/studies")
    public ApiResponse<MyStudiesListResponse> getMyStudiesList(
            @Valid @ModelAttribute MyStudiesQueryRequest query
    ) {
        // ⚠️ TODO: 임시 userId = 1
        Integer userId = 1;

        // 1. UserService의 스터디 목록 조회 메서드 호출
        MyStudiesListResponse responseDto = userService.getMyStudies(userId, query);

        // 2. 200 OK 응답 반환 (메시지 커스텀 적용)
        return ApiResponse.onSuccess(responseDto, "내 스터디 목록 조회 성공");
    }
    /**
     * API 3-4: 포인트 내역 조회
     * URL: GET /api/users/me/point-history?page=...&size=...
     * @param query (page, size) - @ModelAttribute로 Query Parameter 바인딩
     * @return 200 OK (PointHistoryListResponse)
     */
    @GetMapping("/me/point-history")
    public ApiResponse<PointHistoryListResponse> getPointHistoryList(
            @Valid @ModelAttribute PointHistoryQueryRequest query // ⭐️ @ModelAttribute 사용
    ) {
        // ⚠️ TODO: 임시 userId = 1
        Integer userId = 1;

        // 1. UserService의 포인트 내역 조회 메서드 호출
        PointHistoryListResponse responseDto = userService.getPointHistory(userId, query);

        // 2. 200 OK 응답 반환 (커스텀 메시지 적용)
        return ApiResponse.onSuccess(responseDto, "포인트 내역 조회 성공");
    }
    /**
     * API 3-5: 신뢰도 내역 조회
     * URL: GET /api/users/me/reliability-history?page=...&size=...
     * @param query (page, size) - @ModelAttribute로 Query Parameter 바인딩
     * @return 200 OK (ReliabilityHistoryListResponse)
     */
    @GetMapping("/me/reliability-history")
    public ApiResponse<ReliabilityHistoryListResponse> getReliabilityHistoryList(
            @Valid @ModelAttribute ReliabilityHistoryQueryRequest query
    ) {
        // ⚠️ TODO: 임시 userId = 1
        Integer userId = 1;

        // 1. UserService 호출
        ReliabilityHistoryListResponse responseDto = userService.getReliabilityHistory(userId, query);

        // 2. 200 OK 응답 반환 (커스텀 메시지 적용)
        return ApiResponse.onSuccess(responseDto, "신뢰도 내역 조회 성공");
    }

}