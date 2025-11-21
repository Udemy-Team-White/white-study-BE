package teamprojects.demo.dto.study;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyCreateRequest {

    // ✅ 필수 1. 제목
    @NotBlank(message = "스터디 제목은 필수입니다.")
    @Size(max = 100, message = "스터디 제목은 100자 이내여야 합니다.")
    private String title;

    // ✅ 필수 2. 내용
    @NotBlank(message = "스터디 상세 내용은 필수입니다.")
    private String content;

    // 🔻 선택 1. 스터디 진행 방식 (검증 삭제 -> Null 허용)
    private String studyType;

    // 🔻 선택 2. 카테고리 ID 목록 (검증 삭제 -> Null 허용)
    private List<Integer> categoryIds;

    // ✅ 필수 3. 모집 인원
    @NotNull(message = "모집 최대 인원은 필수입니다.")
    @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 50, message = "모집 인원은 50명 이하로 설정해야 합니다.")
    private Integer maxMembers;

    // ✅ 필수 4. 모집 마감일
    @NotNull(message = "모집 마감일은 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Future(message = "모집 마감일은 현재 시간 이후여야 합니다.")
    private LocalDateTime closedAt;

    // 🔻 선택 3. 스터디 시작일
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime startDate;
}