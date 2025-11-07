package teamprojects.demo.global.common.code.status;

import teamprojects.demo.global.common.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // --- 공통 에러 (⭐️ HttpStatus 추가!) ---
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal Server Error"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "Bad Request"),

    // --- 회원가입(API 1-1) 관련 에러 ---
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "이미 사용 중인 이메일입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "이미 사용 중인 닉네임입니다."); // ⭐️ 세미콜론(;) 확인

    // === 필드 3개 ===
    private final HttpStatus httpStatus;

    // ⭐️ 1. 타입을 'Integer' (객체)로 선언
    private final Integer code;

    private final String message;

    // === BaseErrorCode 인터페이스 구현 ===

    // ⭐️ 2. 반환 타입을 'Integer' (객체)로 변경
    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}