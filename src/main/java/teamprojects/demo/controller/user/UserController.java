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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import teamprojects.demo.dto.user.InventoryListRequest;
import teamprojects.demo.dto.user.InventoryListResponse;
import teamprojects.demo.dto.user.InventoryStatusUpdateRequest;
import teamprojects.demo.dto.user.InventoryStatusUpdateResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users") //User 도메인 공통 URL Prefix
public class UserController {


    private final UserService userService;

    // 로그인한 유저 ID 가져오기 (없으면 401 에러 발생)
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

        // 로그인 확인 (토큰 검사 -> 없으면 401)
        getAuthedUserId();

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
     * API 3-2-3: 프로필 이미지 수정
     * URL: POST /api/users/me/profile-image
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public ApiResponse<String> updateProfileImage(
            @RequestPart(value = "image") MultipartFile image) { // @RequestPart 사용

        Integer userId = getAuthedUserId();

        try {
            String imageUrl = userService.updateProfileImage(userId, image);
            return ApiResponse.onSuccess(imageUrl, "프로필 이미지가 수정되었습니다.");
        } catch (IOException e) {
            return ApiResponse.onFailure(500, "이미지 업로드 실패");
        }
    }

    /**
     * API 3-3: 내 스터디 목록 조회
     * URL: GET /api/users/me/studies
     */
    @GetMapping("/me/studies")
    public ApiResponse<MyStudiesListResponse> getMyStudiesList(
            @Valid @ModelAttribute MyStudiesQueryRequest query
    ) {
        // 로그인한 유저 ID 사용
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
        // 로그인한 유저 ID 사용
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
        // 로그인한 유저 ID 사용
        Integer userId = getAuthedUserId();

        ReliabilityHistoryListResponse responseDto = userService.getReliabilityHistory(userId, query);
        return ApiResponse.onSuccess(responseDto, "신뢰도 내역 조회 성공");
    }

    /**
     * API 6-3: 내 인벤토리 조회
     * URL: GET /api/users/me/inventory
     */
    @GetMapping("/me/inventory")
    public ApiResponse<InventoryListResponse> getMyInventory(
            @ModelAttribute InventoryListRequest request) { // Query Parameter

        Integer userId = getAuthedUserId();

        InventoryListResponse responseDto = userService.getMyInventory(userId, request);

        return ApiResponse.onSuccess(responseDto, "내 인벤토리 조회 성공");
    }

    /**
     * API 6-4: 아이템 장착/해제
     * URL: PATCH /api/users/me/inventory/{inventoryId}/status
     */
    @PatchMapping("/me/inventory/{inventoryId}/status")
    public ApiResponse<InventoryStatusUpdateResponse> updateInventoryStatus(
            @PathVariable Integer inventoryId,
            @Valid @RequestBody InventoryStatusUpdateRequest request) {

        InventoryStatusUpdateResponse responseDto = userService.updateInventoryStatus(inventoryId, request);

        return ApiResponse.onSuccess(responseDto, "아이템 상태가 변경되었습니다.");
    }

}