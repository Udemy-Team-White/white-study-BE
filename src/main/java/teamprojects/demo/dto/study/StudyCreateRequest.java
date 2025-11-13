package teamprojects.demo.dto.study;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyCreateRequest {

    @NotBlank(message = "스터디 제목은 필수입니다.")
    @Size(max = 100, message = "스터디 제목은 100자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "스터디 상세 내용은 필수입니다.")
    private String content; // TEXT 타입이므로 길이에 대한 @Size는 생략합니다.

    @NotBlank(message = "스터디 진행 방식(studyType)은 필수입니다.")
    @Size(max = 50, message = "스터디 진행 방식은 50자 이내여야 합니다.")
    // ⭐️ 참고: 실제로는 Enum 값(ONLINE, OFFLINE, MIXED) 검증 로직이 Service Layer에 필요합니다.
    private String studyType;

    @NotNull(message = "카테고리는 최소 1개 이상 선택해야 합니다.")
    @Size(min = 1, message = "카테고리는 최소 1개 이상 선택해야 합니다.")
    private List<Integer> categoryIds; // N:M 처리를 위한 ID 목록

    @NotNull(message = "모집 최대 인원은 필수입니다.")
    @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 50, message = "모집 인원은 50명 이하로 설정해야 합니다.")
    private Integer maxMembers;

    @NotNull(message = "모집 마감일은 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // 명세서 형식 지정
    @Future(message = "모집 마감일은 현재 시간 이후여야 합니다.") // 모집 마감일이 과거일 수 없습니다.
    private LocalDateTime closedAt; // "string (DATETIME, 'YYYY-MM-DDTHH:mm:ss')" -> LocalDateTime으로 받습니다.

    // Optional 필드는 @NotNull을 제거합니다.
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate; // 스터디 시작일
}