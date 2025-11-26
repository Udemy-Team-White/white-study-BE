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
@RequestMapping("/api/todo-items") //요청 주소: /api/todo-items
public class TodoItemController {

    private final StudyService studyService;

    /**
     * API 4-5: TODO 항목 완료 상태 변경 (체크/언체크)
     * URL: PATCH /api/todo-items/{itemId}/status
     */
    @PatchMapping("/{itemId}/status")
    public ApiResponse<TodoItemStatusResponse> updateTodoItemStatus(
            @PathVariable("itemId") Integer itemId,
            @Valid @RequestBody TodoItemUpdateRequest request) {

        TodoItemStatusResponse responseDto = studyService.updateTodoItemStatus(itemId, request);

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