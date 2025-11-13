package teamprojects.demo.service.user;

import teamprojects.demo.dto.auth.AuthRegisterRequest;
import teamprojects.demo.entity.User;
import teamprojects.demo.entity.UserProfile;
import teamprojects.demo.global.common.code.status.ErrorStatus;
import teamprojects.demo.global.common.exception.CustomException;
import teamprojects.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamprojects.demo.repository.StudyApplicationRepository;
import teamprojects.demo.repository.UserProfileRepository;
import teamprojects.demo.dto.auth.AuthLoginRequest;
import teamprojects.demo.dto.auth.AuthLoginResponse;
import java.util.UUID; //Salt 생성을 위해 UUID import


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    // DB 접근을 위한 UserRepository 의존성 주입
    private final UserRepository userRepository;

    // 비밀번호 암호화를 위한 PasswordEncoder 의존성 주입
    private final PasswordEncoder passwordEncoder;

    private final UserProfileRepository userProfileRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    /**
     * 회원 가입 로직 (API 1-1)
     * @param request (email, password, username)
     * @return User Entity (저장된 사용자 정보)
     */
    @Transactional // 쓰기 작업이므로 @Transactional 어노테이션 추가
    public User signUp(AuthRegisterRequest request) {

        // 1. 이메일 중복 확인 (UserRepository에 선언된 메서드 사용)
        if (userRepository.existsByEmail(request.getEmail())) {
            // 이미 사용 중인 이메일일 경우, 409 Conflict 에러 발생
            throw new CustomException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            // (ErrorStatus에 USERNAME_ALREADY_EXISTS Enum 값을 추가했다고 가정합니다)
            throw new CustomException(ErrorStatus.USERNAME_ALREADY_EXISTS);
        }

        // 2. Salt (랜덤 UUID) 생성 (ERD의 salt 컬럼에 저장할 값)
        String salt = UUID.randomUUID().toString();

        // 3. 비밀번호 암호화 (Bcrypt 사용)
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. DTO를 Entity로 변환 (AuthRegisterRequest의 toEntity 메서드 사용)
        User newUser = request.toEntity(encodedPassword, salt);

        // 5. DB에 저장 (save)
        return userRepository.save(newUser);
    }
    public AuthLoginResponse login(AuthLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorStatus.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorStatus.LOGIN_FAILED);
        }

        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        Integer requestCount = studyApplicationRepository.countPendingApplicationsByLeaderId(user.getId());

        String accessToken = "eyJhbGciOiJI...";

        return AuthLoginResponse.builder()
                .accessToken(accessToken)
                .userProfile(AuthLoginResponse.UserProfileDto.builder()
                        .username(user.getUsername())
                        .points(userProfile.getPoints())
                        .reliabilityScore(userProfile.getReliabilityScore())
                        .studyRequestCount(requestCount)
                        .build())
                .build();
    }
}