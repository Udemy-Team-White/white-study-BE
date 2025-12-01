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
import teamprojects.demo.dto.user.UserNicknameUpdateRequest;
import teamprojects.demo.dto.user.UserPasswordUpdateRequest;
import teamprojects.demo.dto.user.UserProfileUpdateResponse;


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
     * API 3-2-1: 닉네임 수정
     * URL: PATCH /api/users/me/username
     */
    @PatchMapping("/me/username")
    public ApiResponse<UserProfileUpdateResponse> updateNickname(
            @Valid @RequestBody UserNicknameUpdateRequest request) {

        Integer userId = getAuthedUserId();

        User updatedUser = userService.updateNickname(userId, request);

        UserProfileUpdateResponse response = UserProfileUpdateResponse.builder()
                .username(updatedUser.getUsername())
                .build();

        return ApiResponse.onSuccess(response, "닉네임이 성공적으로 수정되었습니다.");
    }

    /**
     * API 3-2-2: 비밀번호 수정
     * URL: PATCH /api/users/me/password
     */
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(
            @Valid @RequestBody UserPasswordUpdateRequest request) {

        Integer userId = getAuthedUserId();

        userService.updatePassword(userId, request);

        // 비밀번호 변경은 데이터 반환 없이 메시지만 줍니다.
        return ApiResponse.onSuccess(null, "비밀번호가 성공적으로 수정되었습니다.");
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