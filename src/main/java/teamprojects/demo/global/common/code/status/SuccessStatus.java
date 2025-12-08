package teamprojects.demo.global.common.code.status;

import teamprojects.demo.global.common.code.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements BaseCode {

    //200
    _OK(200, "OK"),

    //201
    _CREATED(201, "회원가입이 성공적으로 완료되었습니다.");

    private final Integer code;
    private final String message;
}