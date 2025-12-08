package teamprojects.demo.controller.study;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import teamprojects.demo.dto.study.TodoItemCreateRequest;
import teamprojects.demo.dto.study.TodoItemResponse;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.study.StudyService;
import jakarta.validation.Valid;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo-lists") // 주소: /api/todo-lists
public class TodoListController {

    private final StudyService studyService;

    /**
     * API 4-4: TODO 항목 생성
     * URL: POST /api/todo-lists/{listId}/items
     */
    @PostMapping("/{listId}/items")
    public ApiResponse<TodoItemResponse> createTodoItem(
            @PathVariable("listId") Integer listId,
            @Valid @RequestBody TodoItemCreateRequest request) {

        TodoItemResponse responseDto = studyService.createTodoItem(listId, request);

        return ApiResponse.onCreated(responseDto, "TODO 항목이 추가되었습니다.");
    }
}