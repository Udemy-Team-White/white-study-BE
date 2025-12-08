package teamprojects.demo.dto.study;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyMemberResponse {

    private String myStatus; // 조회하는 사람의 상태 (LEADER, MEMBER 등)
    private List<MemberDto> members;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDto {
        private Integer memberId;
        private String role;
        private UserDto user;
        private Boolean isMe;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Integer userId;
        private String username;
        private String email;
        private String bio;
        private String imgUrl;
        // 착용 아이템 목록
        private List<String> equippedItems;
    }
}
