package teamprojects.demo.global.common.code.status;

import teamprojects.demo.global.common.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // --- 공통 에러 ---
    _INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    _BAD_REQUEST(400, "Bad Request"),

    // --- 회원가입(API 1-1) 관련 에러 ---
    EMAIL_ALREADY_EXISTS(409, "이미 사용 중인 이메일입니다.");

    // (참고: API 1-1의 409 에러 메시지가 "이미 사용 중인 이메일 또는 닉네임입니다." 라면
    // USERNAME_ALREADY_EXISTS(409, "이미 사용 중인 닉네임입니다.") 도 추가할 수 있습니다.)

    private final Integer code;
    private final String message;
}