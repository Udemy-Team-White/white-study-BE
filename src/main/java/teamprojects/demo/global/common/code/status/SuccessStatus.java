package teamprojects.demo.global.common.code.status;

import teamprojects.demo.global.common.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {

    // --- 공통 성공 코드 ---
    _OK(200, "OK"),

    // --- 회원가입(API 1-1) 성공 코드 ---
    // (명세에 정의된 201 메시지를 사용합니다)
    _CREATED(201, "회원가입이 성공적으로 완료되었습니다.");

    private final Integer code;
    private final String message;
}