package teamprojects.demo.global.exception;

import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.global.common.code.BaseErrorCode;
import teamprojects.demo.global.common.exception.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus; // (참고용 import)

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ex) {

        BaseErrorCode errorCode = ex.getErrorCode();

        // ⭐️ 1. (수정됨!) 님의 onFailure는 인자 2개만 받습니다.
        ApiResponse<Void> response = ApiResponse.onFailure(
                errorCode.getCode(),       // 1번째 인자 (int 또는 Integer)
                errorCode.getMessage()     // 2번째 인자 (String)
                // (3번째 인자 'null' 제거!)
        );

        // ⭐️ 2. (이제 작동함!) BaseErrorCode에 getHttpStatus()가 추가되었기 때문
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }
}