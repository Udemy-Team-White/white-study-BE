package teamprojects.demo.controller.study;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamprojects.demo.dto.category.CategoryResponse;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.study.StudyService; // ⭐️ 선생님의 StudyService 사용

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    // 기존 StudyService를 주입
    private final StudyService studyService;

    /**
     * 카테고리 목록 조회
     * [GET] /api/categories
     */
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getCategories() {

        // StudyService에 방금 추가한 메서드 호출
        List<CategoryResponse> response = studyService.getAllCategories();

        return ApiResponse.onSuccess(response);
    }
}
