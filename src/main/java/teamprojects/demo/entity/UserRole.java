package teamprojects.demo.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("사용자"),
    MANAGER("스터디장"),
    ADMIN("관리자");

    private final String description;
}