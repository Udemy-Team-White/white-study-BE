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
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "이미 사용 중인 닉네임입니다."), // ⭐️ 세미콜론(;) 확인
    // 3. 이미 멤버이거나 신청 대기 중일 때 (409 Conflict)
    ALREADY_MEMBER_OR_APPLIED(HttpStatus.CONFLICT, 409, "이미 신청했거나 참여 중인 스터디입니다."),


    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, 400, "아이디 혹은 비밀번호가 다릅니다!"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "로그인 후 다시 시도해 주십시오."),

    // ⭐️ 2. 유효성 검사 실패 (카테고리 ID 등 잘못된 데이터, 400 Bad Request)
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, 400, "요청한 카테고리 ID가 존재하지 않습니다."),
    // 2. 모집 마감된 스터디에 신청 시도 (400 Bad Request)
    RECRUITMENT_CLOSED(HttpStatus.BAD_REQUEST, 400, "모집이 마감된 스터디입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, 400, "현재 비밀번호가 일치하지 않습니다."),


    _FORBIDDEN(HttpStatus.FORBIDDEN, 403, "접근 권한이 없습니다."),

    // 스터디가 없을 때 404 에러
    _NOT_FOUND(HttpStatus.NOT_FOUND, 404, "요청한 자원을 찾을 수 없습니다."),
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "해당 스터디를 찾을 수 없습니다.");
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