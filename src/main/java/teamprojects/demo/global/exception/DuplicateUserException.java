package teamprojects.demo.global.exception;

// RuntimeException을 상속받습니다.
public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
}