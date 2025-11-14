// teamprojects.demo.dto.user.UserProfileUpdateResponse.java

package teamprojects.demo.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateResponse {

    // "username": "string (변경된 새 닉네임)"
    private String username;
}
