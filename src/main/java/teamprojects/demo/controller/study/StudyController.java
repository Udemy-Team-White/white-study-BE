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

@RestController // ⭐️ REST API 컨트롤러
@RequiredArgsConstructor
@RequestMapping("/api/studies") // ⭐️ 기본 URL: /api/studies
public class StudyController {

    private final UserService userService;
    private final StudyService studyService; // ⭐️ 새로 만든 StudyService 주입

    /**
     * API 1-5: 스터디 목록 조회 (검색, 필터링, 페이지네이션)
     * URL: GET /api/studies?page=0&size=12...
     * @param request (StudyListRequest DTO에 Query Parameters가 바인딩됨)
     * @return 200 OK (StudyListResponse)
     */
    @GetMapping("") // 최종 URL: /api/studies
    public ApiResponse<StudyListResponse> getStudyList(@Valid @ModelAttribute StudyListRequest request) {

        // 1. StudyService의 목록 조회 메서드 호출
        StudyListResponse responseDto = studyService.getStudyList(request);

        // 2. 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto);
    }
    /**
     * API 2-1: 스터디 개설 요청
     * URL: POST /api/studies
     * @param request (StudyCreateRequest DTO에 JSON Request Body가 바인딩됨)
     * @return 201 Created (StudyCreateResponse)
     */
    @PostMapping("") // 최종 URL: /api/studies
    public ApiResponse<StudyCreateResponse> createStudy(@Valid @RequestBody StudyCreateRequest request) {

        // ⭐️ [디버깅용 로그] 서버 로그(app.log)에 이 줄이 찍히나 봐야 합니다.
        System.out.println("========== 스터디 개설 요청 들어옴! 제목: " + request.getTitle() + " ==========");

        // 1. StudyService의 스터디 개설 메서드 호출
        StudyCreateResponse responseDto = studyService.createStudy(request);

        // 2. 201 Created 응답 반환
        return ApiResponse.onCreated(responseDto);
    }
    /**
     * API 2-2: 스터디 상세 정보 조회
     * URL: GET /api/studies/{studyId}
     * @param studyId (URL Path Variable)
     * @return 200 OK (StudyDetailResponse)
     */
    @GetMapping("/{studyId}") // 최종 URL: /api/studies/{studyId}
    public ApiResponse<StudyDetailResponse> getStudyDetail(@PathVariable Integer studyId) {

        // 1. StudyService의 상세 조회 메서드 호출
        StudyDetailResponse responseDto = studyService.getStudyDetail(studyId);

        // 2. 200 OK 응답 반환
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

        // 1. StudyService의 신청 메서드 호출
        StudyApplyResponse responseDto = studyService.applyToStudy(studyId, request);

        // 2. 201 Created 응답 반환
        return ApiResponse.onCreated(responseDto);
    }
    /**
     * API 4-1: 스터디 대시보드 초기 데이터 조회
     * URL: GET /api/studies/{studyId}/dashboard (✅ 최종 확정 URL)
     * @param studyId Path Variable로 전달된 스터디 ID
     * @return 200 OK (StudyDashboardResponse)
     */
    @GetMapping("/{studyId}/dashboard")
    public ApiResponse<StudyDashboardResponse> getStudyDashboardData(
            @PathVariable("studyId") Integer studyId
            // ⚠️ @AuthenticationPrincipal은 나중에 추가
    ) {
        // ⚠️ TODO: 현재는 로그인이 구현되지 않았으므로, 테스트를 위해 userId를 1로 고정합니다.
        Integer userId = 1;

        // 1. UserService의 대시보드 조회 메서드 호출
        StudyDashboardResponse responseDto = userService.getDashboardData(studyId, userId);

        // 2. 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto, "스터디 대시보드 정보 조회 성공");
    }

    /**
     * API 4-2: TODO 플래너 조회 (날짜별)
     * URL: GET /api/studies/{studyId}/todos?date=YYYY-MM-DD
     * (로그인 필요)
     * @param studyId (Path Variable)
     * @param date (Query Parameter, YYYY-MM-DD 형식)
     * @return 200 OK (List<TodoPlannerResponse>)
     */
    @GetMapping("/{studyId}/todos")
    public ApiResponse<List<TodoPlannerResponse>> getStudyTodos(
            @PathVariable Integer studyId,
            @RequestParam LocalDate date) { // ⭐️ String "YYYY-MM-DD"가 자동으로 LocalDate로 변환됩니다.

        // 1. StudyService 호출
        List<TodoPlannerResponse> responseDto = studyService.getStudyTodos(studyId, date);

        // 2. 200 OK 응답 반환
        return ApiResponse.onSuccess(responseDto);
    }
    /**
     * API 4-3: TODO 플래너(그룹) 생성
     * URL: POST /api/studies/{studyId}/todo-lists
     * (로그인 필요, 스터디 멤버만 가능)
     * @param studyId (Path Variable)
     * @param request (JSON Body: targetDate, title)
     * @return 201 Created (TodoListCreateResponse)
     */
    @PostMapping("/{studyId}/todo-lists")
    public ApiResponse<TodoListCreateResponse> createTodoList(
            @PathVariable Integer studyId,
            @Valid @RequestBody TodoListCreateRequest request) {

        // 1. StudyService의 생성 메서드 호출
        TodoListCreateResponse responseDto = studyService.createTodoList(studyId, request);

        // 2. 201 Created 응답 반환
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
}