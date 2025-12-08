package teamprojects.demo.controller.study;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamprojects.demo.dto.study.StudyListRequest;
import teamprojects.demo.dto.study.StudyListResponse;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.study.StudyService;
import teamprojects.demo.dto.study.StudyCreateRequest;
import teamprojects.demo.dto.study.StudyCreateResponse;
import teamprojects.demo.dto.study.StudyDetailResponse;
import org.springframework.web.bind.annotation.PathVariable;
import teamprojects.demo.dto.study.StudyApplyRequest;
import teamprojects.demo.dto.study.StudyApplyResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import teamprojects.demo.dto.study.TodoPlannerResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import teamprojects.demo.dto.study.TodoListCreateRequest;
import teamprojects.demo.dto.study.TodoListCreateResponse;
import teamprojects.demo.dto.study.StudyDashboardResponse;
import teamprojects.demo.service.user.UserService;
import teamprojects.demo.dto.study.*;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import teamprojects.demo.dto.study.StudyScheduleUpdateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import teamprojects.demo.global.common.exception.CustomException;
import teamprojects.demo.global.common.code.status.ErrorStatus;

@RestController //REST API 컨트롤러
@RequiredArgsConstructor
@RequestMapping("/api/studies") //기본 URL: /api/studies
public class StudyController {

    private final UserService userService;
    private final StudyService studyService; // StudyService 주입

    /**
     * API 1-5: 스터디 목록 조회 (검색, 필터링, 페이지네이션)
     * URL: GET /api/studies?page=0&size=12...
     * @param request (StudyListRequest DTO에 Query Parameters가 바인딩됨)
     * @return 200 OK (StudyListResponse)
     */
    @GetMapping("") // URL: /api/studies
    public ApiResponse<StudyListResponse> getStudyList(@Valid @ModelAttribute StudyListRequest request) {

        // StudyService의 목록 조회 메서드 호출
        StudyListResponse responseDto = studyService.getStudyList(request);

        // 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto);
    }
    /**
     * API 2-1: 스터디 개설 요청
     * URL: POST /api/studies
     * @param request (StudyCreateRequest DTO에 JSON Request Body가 바인딩됨)
     * @return 201 Created (StudyCreateResponse)
     */
    @PostMapping("") // URL: /api/studies
    public ApiResponse<StudyCreateResponse> createStudy(@Valid @RequestBody StudyCreateRequest request) {

        // StudyService의 스터디 개설 메서드 호출
        StudyCreateResponse responseDto = studyService.createStudy(request);

        // 201 Created 응답 반환
        return ApiResponse.onCreated(responseDto);
    }
    /**
     * API 2-2: 스터디 상세 정보 조회
     * URL: GET /api/studies/{studyId}
     * @param studyId (URL Path Variable)
     * @return 200 OK (StudyDetailResponse)
     */
    @GetMapping("/{studyId}") // URL: /api/studies/{studyId}
    public ApiResponse<StudyDetailResponse> getStudyDetail(@PathVariable Integer studyId) {

        // StudyService의 상세 조회 메서드 호출
        StudyDetailResponse responseDto = studyService.getStudyDetail(studyId);

        // 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto);
    }

    /**
     * API 2-3: 스터디 참여 신청
     * URL: POST /api/studies/{studyId}/apply
     * (로그인 필요)
     * @param studyId (URL Path Variable)
     * @param request (신청 메시지 포함)
     * @return 201 Created (StudyApplyResponse)
     */
    @PostMapping("/{studyId}/apply")
    public ApiResponse<StudyApplyResponse> applyToStudy(
            @PathVariable @NotNull Integer studyId,
            @RequestBody @Valid StudyApplyRequest request) { // RequestBody 유효성 검사

        // StudyService의 신청 메서드 호출
        StudyApplyResponse responseDto = studyService.applyToStudy(studyId, request);

        // 201 Created 응답 반환
        return ApiResponse.onCreated(responseDto);
    }
    /**
     * API 4-1: 스터디 대시보드 초기 데이터 조회
     */
    @GetMapping("/{studyId}/dashboard")
    public ApiResponse<StudyDashboardResponse> getStudyDashboardData(@PathVariable("studyId") Integer studyId) {

        // 로그인한 유저 ID 가져오기
        Integer userId = getAuthedUserId();

        // UserService 호출 (기존 StudyService 아님)
        StudyDashboardResponse responseDto = userService.getDashboardData(studyId, userId);

        return ApiResponse.onSuccess(responseDto);
    }

    /**
     * API 4-2: TODO 플래너 조회
     */
    @GetMapping("/{studyId}/todos")
    //  List<TodoPlannerResponse> -> TodoPlannerResponse
    public ApiResponse<TodoPlannerResponse> getStudyTodos(
            @PathVariable Integer studyId,
            @RequestParam LocalDate date) {

        // 서비스 호출
        TodoPlannerResponse responseDto = studyService.getStudyTodos(studyId, date);

        return ApiResponse.onSuccess(responseDto);
    }
    /**
     * API 4-3: TODO 플래너(그룹) 생성
     * URL: POST /api/studies/{studyId}/todo-lists
     */
    @PostMapping("/{studyId}/todo-lists")
    public ApiResponse<TodoListCreateResponse> createTodoList(
            @PathVariable Integer studyId,
            @RequestBody TodoListCreateRequest request) {

        TodoListCreateResponse responseDto = studyService.createTodoList(studyId, request);

        return ApiResponse.onCreated(responseDto);
    }
    /**
     * API 4-7: 셀프 보고서 제출
     * URL: POST /api/studies/{studyId}/reports
     */
    @PostMapping("/{studyId}/reports")
    public ApiResponse<SelfReportRewardResponse> submitSelfReport(
            @PathVariable("studyId") Integer studyId,
            @Valid @RequestBody SelfReportCreateRequest request) {

        SelfReportRewardResponse responseDto = studyService.submitSelfReport(studyId, request);

        // 150자 이상 작성 시 메시지 변경 (reward가 null인지 아닌지로 판단)
        String message = responseDto.getReward() != null
                ? "셀프 보고서가 제출되었습니다. 150자 이상 작성 보상 (신뢰도 +1)이 지급되었습니다!"
                : "셀프 보고서가 제출되었습니다.";

        return ApiResponse.onCreated(responseDto, message);
    }
    /**
     * API 4-8: 셀프 보고서 목록 조회
     * URL: GET /api/studies/{studyId}/reports?page=0&size=20
     */
    @GetMapping("/{studyId}/reports")
    public ApiResponse<SelfReportListResponse> getSelfReportList(
            @PathVariable("studyId") Integer studyId,
            @Valid @ModelAttribute SelfReportListRequest request) { // Query Parameter 받기

        SelfReportListResponse responseDto = studyService.getSelfReportList(studyId, request);

        return ApiResponse.onSuccess(responseDto, "셀프 보고서 목록 조회 성공");
    }

    /**
     * API 5-1: 스터디 신청자 목록 조회
     * URL: GET /api/studies/{studyId}/applicants
     * (스터디장만 가능)
     */
    @GetMapping("/{studyId}/applicants")
    public ApiResponse<List<StudyApplicantResponse>> getApplicants(
            @PathVariable Integer studyId) {

        // (Status는 PENDING 고정이므로 파라미터로 안 받아도 됨)
        List<StudyApplicantResponse> responseDto = studyService.getApplicants(studyId);

        return ApiResponse.onSuccess(responseDto, "신청자 목록 조회 성공");
    }

    /**
     * API 5-2: 스터디 신청 승인
     * URL: POST /api/studies/{studyId}/applicants/{applicationId}/approve
     */
    @PostMapping("/{studyId}/applicants/{applicationId}/approve")
    public ApiResponse<StudyApplicantApproveResponse> approveApplicant(
            @PathVariable Integer studyId,
            @PathVariable Integer applicationId,
            @RequestBody(required = false) StudyApplicantApproveRequest request) { // Body는 선택 사항

        // request가 null일 경우를 대비해 빈 객체 생성
        if (request == null) request = new StudyApplicantApproveRequest();

        StudyApplicantApproveResponse responseDto = studyService.approveApplicant(studyId, applicationId, request);

        return ApiResponse.onSuccess(responseDto, "신청이 승인되었습니다. 새 멤버로 추가되었습니다.");
    }

    /**
     * API 5-3: 스터디 신청 거절
     * URL: POST /api/studies/{studyId}/applicants/{applicationId}/reject
     */
    @PostMapping("/{studyId}/applicants/{applicationId}/reject")
    public ApiResponse<StudyApplicantRejectResponse> rejectApplicant(
            @PathVariable Integer studyId,
            @PathVariable Integer applicationId,
            @RequestBody(required = false) StudyApplicantRejectRequest request) {

        if (request == null) request = new StudyApplicantRejectRequest();

        StudyApplicantRejectResponse responseDto = studyService.rejectApplicant(studyId, applicationId, request);

        return ApiResponse.onSuccess(responseDto, "신청이 거절되었습니다.");
    }

    /**
     * API 5-4: 확정 멤버 목록 조회
     * URL: GET /api/studies/{studyId}/members
     */
    @GetMapping("/{studyId}/members")
    public ApiResponse<StudyMemberResponse> getStudyMembers(@PathVariable Integer studyId) {

        StudyMemberResponse responseDto = studyService.getStudyMembers(studyId);

        return ApiResponse.onSuccess(responseDto, "확정 멤버 목록 조회 성공");
    }

    /**
     * API 5-5: 멤버 역할 변경
     * URL: PATCH /api/studies/{studyId}/members/{memberId}
     */
    @PatchMapping("/{studyId}/members/{memberId}")
    public ApiResponse<StudyMemberRoleUpdateResponse> updateMemberRole(
            @PathVariable Integer studyId,
            @PathVariable Integer memberId,
            @Valid @RequestBody StudyMemberRoleUpdateRequest request) {

        StudyMemberRoleUpdateResponse responseDto = studyService.updateMemberRole(studyId, memberId, request);

        return ApiResponse.onSuccess(responseDto, "멤버 역할이 성공적으로 변경되었습니다.");
    }

    /**
     * API 5-6: 스터디 멤버 추방
     * URL: DELETE /api/studies/{studyId}/members/{memberId}
     */
    @DeleteMapping("/{studyId}/members/{memberId}")
    public ApiResponse<StudyMemberKickResponse> kickMember(
            @PathVariable Integer studyId,
            @PathVariable Integer memberId) {

        StudyMemberKickResponse responseDto = studyService.kickMember(studyId, memberId);

        return ApiResponse.onSuccess(responseDto, "멤버를 스터디에서 추방했습니다.");
    }

    /**
     * API 5-7: 스터디 정보 수정
     * URL: POST /api/studies/{studyId} (팀원 요청으로 POST 사용)
     */
    @PostMapping("/{studyId}")
    public ApiResponse<Map<String, Integer>> updateStudy(
            @PathVariable Integer studyId,
            @RequestBody StudyUpdateRequest request) {

        studyService.updateStudy(studyId, request);

        // 응답 데이터 생성
        Map<String, Integer> data = new HashMap<>();
        data.put("studyId", studyId);

        return ApiResponse.onSuccess(data, "스터디 정보가 성공적으로 수정되었습니다.");
    }

    /**
     * API 5-8: 스터디 일정 및 주기 수정
     * URL: PATCH /api/studies/{studyId}/schedule
     */
    @PatchMapping("/{studyId}/schedule")
    public ApiResponse<String> updateStudySchedule(
            @PathVariable Integer studyId,
            @RequestBody StudyScheduleUpdateRequest request) {

        studyService.updateStudySchedule(studyId, request);

        return ApiResponse.onSuccess("스터디 일정 및 주기가 수정되었습니다.");
    }

    /**
     * API 6-5: 칭찬 메시지 전송
     * URL: POST /api/studies/{studyId}/praises
     */
    @PostMapping("/{studyId}/praises")
    public ApiResponse<PraiseResponse> sendPraise(
            @PathVariable Integer studyId,
            @Valid @RequestBody PraiseCreateRequest request) {

        PraiseResponse responseDto = studyService.sendPraise(studyId, request);

        return ApiResponse.onCreated(responseDto, "칭찬 메시지가 익명으로 전송되었습니다. (보상: 포인트 +10)");
    }

    private Integer getAuthedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null || authentication.getPrincipal().equals("anonymousUser")) {
            // 로그인 안 된 상태면 에러 발생
            throw new CustomException(ErrorStatus.UNAUTHORIZED);
            // 만약 CustomException이 없다면: throw new RuntimeException("로그인이 필요합니다.");
        }

        // JwtFilter에서 넣은 userId(Integer)를 꺼냅니다.
        return (Integer) authentication.getPrincipal();
    }
}