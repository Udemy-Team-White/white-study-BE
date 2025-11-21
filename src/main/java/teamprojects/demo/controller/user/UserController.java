package teamprojects.demo.controller.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import teamprojects.demo.dto.user.*;
import teamprojects.demo.dto.study.MyStudiesListResponse;
import teamprojects.demo.dto.study.MyStudiesQueryRequest;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.user.UserService;
import teamprojects.demo.entity.User;
import teamprojects.demo.global.utils.SecurityUtils;
import teamprojects.demo.global.common.exception.CustomException;
import teamprojects.demo.global.common.code.status.ErrorStatus;
import jakarta.validation.Valid;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users") // ⭐️ User 도메인 공통 URL Prefix
public class UserController {


    private final UserService userService;

    // ⭐️ [헬퍼 메서드] 로그인한 유저 ID 가져오기 (없으면 401 에러 발생)
    private Integer getAuthedUserId() {
        return SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
    }

    /**
     * API 3-1: 마이페이지 초기 데이터 조회
     * URL: GET /api/users/me/mypage-data
     */
    @GetMapping("/me/mypage-data")
    public ApiResponse<MypageDataResponse> getUserMypageData() {

        // 1. 로그인 확인 (토큰 검사 -> 없으면 401)
        getAuthedUserId();
        // (참고: UserService 내부에서도 SecurityUtils를 쓰고 있다면 파라미터로 안 넘겨도 되지만,
        // 여기서는 컨트롤러 진입 시점에 권한 체크를 확실히 하기 위해 호출해둡니다.)

        MypageDataResponse responseDto = userService.getUserMypageData();
        return ApiResponse.onSuccess(responseDto);
    }

    /**
     * API 3-2: 프로필 수정 요청
     * [PATCH] /api/users/me/profile
     */
    @PatchMapping("/me/profile")
    public ApiResponse<UserProfileUpdateResponse> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        // ⭐️ [수정됨] 진짜 로그인한 유저 ID 가져오기
        Integer userId = getAuthedUserId();

        // 서비스 호출 (진짜 유저 ID 전달)
        User updatedUser = userService.updateProfile(userId, request);

        UserProfileUpdateResponse response = UserProfileUpdateResponse.builder()
                .username(updatedUser.getUsername())
                .build();

        return ApiResponse.onSuccess(response);
    }
    /**
     * API 3-3: 내 스터디 목록 조회
     * URL: GET /api/users/me/studies
     */
    @GetMapping("/me/studies")
    public ApiResponse<MyStudiesListResponse> getMyStudiesList(
            @Valid @ModelAttribute MyStudiesQueryRequest query
    ) {
        // ⭐️ [수정됨] 진짜 로그인한 유저 ID 사용
        Integer userId = getAuthedUserId();

        MyStudiesListResponse responseDto = userService.getMyStudies(userId, query);
        return ApiResponse.onSuccess(responseDto, "내 스터디 목록 조회 성공");
    }
    /**
     * API 3-4: 포인트 내역 조회
     * URL: GET /api/users/me/point-history
     */
    @GetMapping("/me/point-history")
    public ApiResponse<PointHistoryListResponse> getPointHistoryList(
            @Valid @ModelAttribute PointHistoryQueryRequest query
    ) {
        // ⭐️ [수정됨] 진짜 로그인한 유저 ID 사용
        Integer userId = getAuthedUserId();

        PointHistoryListResponse responseDto = userService.getPointHistory(userId, query);
        return ApiResponse.onSuccess(responseDto, "포인트 내역 조회 성공");
    }

    /**
     * API 3-5: 신뢰도 내역 조회
     * URL: GET /api/users/me/reliability-history
     */
    @GetMapping("/me/reliability-history")
    public ApiResponse<ReliabilityHistoryListResponse> getReliabilityHistoryList(
            @Valid @ModelAttribute ReliabilityHistoryQueryRequest query
    ) {
        // ⭐️ [수정됨] 진짜 로그인한 유저 ID 사용
        Integer userId = getAuthedUserId();

        ReliabilityHistoryListResponse responseDto = userService.getReliabilityHistory(userId, query);
        return ApiResponse.onSuccess(responseDto, "신뢰도 내역 조회 성공");
    }

}