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
}