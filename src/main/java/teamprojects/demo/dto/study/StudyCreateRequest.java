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

    // 제목(필수)
    @NotBlank(message = "모집글 제목(title)은 필수입니다.")
    @Size(max = 100, message = "스터디 제목은 100자 이내여야 합니다.")
    private String title;

    //선택 스터디 이름
    private String studyName;

    // 내용(필수)
    @NotBlank(message = "스터디 상세 내용은 필수입니다.")
    private String content;

    private String studyType;
    private List<Integer> categoryIds;

    // 모집 인원(필수)
    @NotNull(message = "모집 최대 인원은 필수입니다.")
    @Min(value = 2, message = "모집 인원은 최소 2명 이상이어야 합니다.")
    @Max(value = 50, message = "모집 인원은 50명 이하로 설정해야 합니다.")
    private Integer maxMembers;

    // 모집 마감일(필수)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Future(message = "모집 마감일은 현재 시간 이후여야 합니다.")
    private LocalDateTime closedAt;

    //  선택 . 시작일 (Null 허용)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime startDate;

    // 선택 . 종료일 (Null 허용)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime endDate;
}