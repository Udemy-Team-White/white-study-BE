package teamprojects.demo.global.common.exception;

import teamprojects.demo.global.common.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final BaseErrorCode errorCode;

    public CustomException(BaseErrorCode errorCode) {
        // 부모의 생성자를 호출하여 에러 메시지를 설정
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}