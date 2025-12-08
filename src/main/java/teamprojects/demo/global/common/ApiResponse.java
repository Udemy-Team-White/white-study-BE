package teamprojects.demo.global.common; // 님의 패키지 경로

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"code", "message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(200, "요청에 성공했습니다.", data);
    }

    // 성공 응답 (데이터 없음, 예: 삭제)
    public static <T> ApiResponse<T> onSuccess() {
        return new ApiResponse<>(200, "요청에 성공했습니다.", null);
    }

    // 생성(201) 응답 (데이터 포함)
    public static <T> ApiResponse<T> onCreated(T data) {
        return new ApiResponse<>(201, "성공적으로 생성되었습니다.", data);
    }

    // 실패 응답 (Service에서 예외 발생 시)
    public static <T> ApiResponse<T> onFailure(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    public static <T> ApiResponse<T> onCreated(T data, String message) {
        return new ApiResponse<>(201, message, data);
    }
    public static <T> ApiResponse<T> onSuccess(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }
}