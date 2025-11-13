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
import teamprojects.demo.dto.study.StudyDetailResponse; // ⭐️ API 2-2 응답 DTO
import org.springframework.web.bind.annotation.PathVariable; // ⭐️ URL 경로 변수({studyId}) 처리를 위함
import teamprojects.demo.dto.study.StudyApplyRequest; // ⭐️ Request DTO
import teamprojects.demo.dto.study.StudyApplyResponse;
import jakarta.validation.constraints.NotNull;// ⭐️ Response DTO
import jakarta.validation.Valid;


@RestController // ⭐️ REST API 컨트롤러
@RequiredArgsConstructor
@RequestMapping("/api/studies") // ⭐️ 기본 URL: /api/studies
public class StudyController {

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
}