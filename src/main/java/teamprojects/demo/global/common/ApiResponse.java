package teamprojects.demo.global.common; // 님의 패키지 경로

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE) // (내부에서만 생성자 사용)
@JsonPropertyOrder({"code", "message", "data"}) // (JSON 순서 고정)
@JsonInclude(JsonInclude.Include.NON_NULL) // (data가 null이면 JSON에서 제외)
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    // 성공 응답 (데이터 포함)
    // (Controller에서 return ApiResponse.onSuccess(data); 로 사용)
    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(200, "요청에 성공했습니다.", data);
    }

    // 성공 응답 (데이터 없음, 예: 삭제)
    // (Controller에서 return ApiResponse.onSuccess(); 로 사용)
    public static <T> ApiResponse<T> onSuccess() {
        return new ApiResponse<>(200, "요청에 성공했습니다.", null);
    }

    // 생성(201) 응답 (데이터 포함)
    public static <T> ApiResponse<T> onCreated(T data) {
        return new ApiResponse<>(201, "성공적으로 생성되었습니다.", data);
    }

    // 실패 응답 (Service에서 예외 발생 시)
    // (ControllerAdvice에서 return ApiResponse.onFailure(code, message); 로 사용)
    public static <T> ApiResponse<T> onFailure(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    public static <T> ApiResponse<T> onCreated(T data, String message) {
        return new ApiResponse<>(201, message, data);
    }
}