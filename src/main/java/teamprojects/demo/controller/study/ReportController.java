package teamprojects.demo.controller.study;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import teamprojects.demo.dto.study.SelfReportDetailResponse;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.study.StudyService;
import teamprojects.demo.dto.study.SelfReportUpdateRequest;
import teamprojects.demo.dto.study.SelfReportUpdateResponse;
import jakarta.validation.Valid;
import teamprojects.demo.dto.study.SelfReportDeleteResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final StudyService studyService;

    /**
     * API 4-9: 셀프 보고서 상세 조회
     * URL: GET /api/reports/{reportId}
     */
    @GetMapping("/{reportId}")
    public ApiResponse<SelfReportDetailResponse> getSelfReportDetail(@PathVariable Integer reportId) {

        SelfReportDetailResponse responseDto = studyService.getSelfReportDetail(reportId);

        return ApiResponse.onSuccess(responseDto, "셀프 보고서 상세 조회 성공");
    }

    /**
     * API 4-10: 셀프 보고서 수정
     * URL: POST /api/reports/{reportId}
     */
    @PostMapping("/{reportId}")
    public ApiResponse<SelfReportUpdateResponse> updateSelfReport(
            @PathVariable Integer reportId,
            @Valid @RequestBody SelfReportUpdateRequest request) {

        SelfReportUpdateResponse responseDto = studyService.updateSelfReport(reportId, request);

        return ApiResponse.onSuccess(responseDto, "셀프 보고서가 수정되었습니다.");
    }

    /**
     * API 4-11: 셀프 보고서 삭제
     * URL: DELETE /api/reports/{reportId}
     */
    @DeleteMapping("/{reportId}")
    public ApiResponse<SelfReportDeleteResponse> deleteSelfReport(@PathVariable Integer reportId) {

        SelfReportDeleteResponse responseDto = studyService.deleteSelfReport(reportId);

        return ApiResponse.onSuccess(responseDto, "셀프 보고서가 삭제되었습니다.");
    }
}
