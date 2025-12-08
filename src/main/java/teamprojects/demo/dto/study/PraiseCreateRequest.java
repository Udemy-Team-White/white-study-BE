package teamprojects.demo.dto.study;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PraiseCreateRequest {

    @NotNull(message = "칭찬받을 멤버 ID는 필수입니다.")
    private Integer receiverId;

    @NotBlank(message = "칭찬 내용은 필수입니다.")
    private String message;
}
