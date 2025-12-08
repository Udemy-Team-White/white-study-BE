package teamprojects.demo.global.common.code.status;

import teamprojects.demo.global.common.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    //공통 에러
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal Server Error"),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, 400, "Bad Request"),

    //409
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "이미 사용 중인 이메일입니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "이미 사용 중인 닉네임입니다."),
    ALREADY_OWNED_ITEM(HttpStatus.CONFLICT, 409, "이미 보유 중인 아이템입니다."),
    ALREADY_MEMBER_OR_APPLIED(HttpStatus.CONFLICT, 409, "이미 신청했거나 참여 중인 스터디입니다."),
    ITEM_ALREADY_EQUIPPED(HttpStatus.CONFLICT, 409, "같은 종류의 아이템은 하나만 장착할 수 있습니다. (기존 아이템을 먼저 해제해주세요.)"),
    PRAISE_LIMIT_EXCEEDED(HttpStatus.CONFLICT, 409, "칭찬하기는 하루에 3번만 가능합니다."),
    REPORT_ALREADY_SUBMITTED(HttpStatus.CONFLICT, 409, "셀프 보고서는 하루에 한 번만 작성할 수 있습니다."),

    //401
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, 401, "아이디 혹은 비밀번호가 다릅니다!"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "로그인 후 다시 시도해 주십시오."),

    //400
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, 400, "요청한 카테고리 ID가 존재하지 않습니다."),
    NOT_ENOUGH_POINTS(HttpStatus.BAD_REQUEST, 400, "포인트가 부족합니다."),
    RECRUITMENT_CLOSED(HttpStatus.BAD_REQUEST, 400, "모집이 마감된 스터디입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, 400, "현재 비밀번호가 일치하지 않습니다."),
    CANNOT_KICK_SELF(HttpStatus.BAD_REQUEST, 400, "스터디장은 스스로를 추방할 수 없습니다."),


    STUDY_TERMINATED(HttpStatus.BAD_REQUEST, 402, "이미 종료된 스터디입니다."),

    //403
    _FORBIDDEN(HttpStatus.FORBIDDEN, 403, "접근 권한이 없습니다."),

    //404
    _NOT_FOUND(HttpStatus.NOT_FOUND, 404, "요청한 자원을 찾을 수 없습니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "존재하지 않는 아이템입니다."),
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 스터디를 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;


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