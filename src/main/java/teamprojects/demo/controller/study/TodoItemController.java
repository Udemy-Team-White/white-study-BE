package teamprojects.demo.controller.study;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import teamprojects.demo.dto.study.TodoItemStatusResponse;
import teamprojects.demo.dto.study.TodoItemUpdateRequest;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.study.StudyService;
import jakarta.validation.Valid;
import teamprojects.demo.dto.study.TodoItemDeleteResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo-items") //주소: /api/todo-items
public class TodoItemController {

    private final StudyService studyService;

    /**
     * API 4-5: TODO 항목 완료 상태 변경
     */
    @PatchMapping("/{itemId}/status")
    public ApiResponse<TodoItemStatusResponse> updateTodoItemStatus(
            @PathVariable("itemId") Integer itemId,
            @RequestBody java.util.Map<String, Object> rawRequest) {

        // 데이터 추출
        Object value = rawRequest.get("isCompleted");
        Boolean isCompleted = false;

        if (value instanceof Boolean) {
            isCompleted = (Boolean) value;
        } else if (value != null) {
            // 문자열 "true"나 "false"로 왔을 경우 처리
            isCompleted = Boolean.parseBoolean(value.toString());
        }

        // DTO 변환
        TodoItemUpdateRequest requestDto = new TodoItemUpdateRequest();
        requestDto.setIsCompleted(isCompleted);

        // 서비스 호출
        TodoItemStatusResponse responseDto = studyService.updateTodoItemStatus(itemId, requestDto);

        return ApiResponse.onSuccess(responseDto, "TODO 항목 상태가 변경되었습니다.");
    }
    /**
     * API 4-6: TODO 항목 삭제
     * URL: DELETE /api/todo-items/{itemId}
     */
    @DeleteMapping("/{itemId}")
    public ApiResponse<TodoItemDeleteResponse> deleteTodoItem(@PathVariable("itemId") Integer itemId) {

        TodoItemDeleteResponse responseDto = studyService.deleteTodoItem(itemId);

        return ApiResponse.onSuccess(responseDto, "TODO 항목이 삭제되었습니다.");
    }
}