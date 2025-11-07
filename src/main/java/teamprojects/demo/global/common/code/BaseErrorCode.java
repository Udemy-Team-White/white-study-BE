package teamprojects.demo.global.common.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    // 1. 에러 코드 (409, 500 등)
    public Integer getCode();

    // 2. 에러 메시지 ("이미 사용 중인 이메일입니다.")
    public String getMessage();

    HttpStatus getHttpStatus();
}