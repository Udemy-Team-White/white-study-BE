package teamprojects.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamprojects.demo.dto.auth.RegisterRequestDto;
import teamprojects.demo.dto.auth.RegisterResponseDto;
import teamprojects.demo.entity.User;
import teamprojects.demo.entity.UserProfile;
import teamprojects.demo.repository.UserProfileRepository;
import teamprojects.demo.repository.UserRepository;

import java.util.UUID; // Salt 생성을 위해

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용, CUD 시 @Transactional 추가
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder; // (SecurityConfig에 등록한 Bean)

    @Transactional // DB에 CUD(Create, Update, Delete) 작업을 하므로 필수!
    public RegisterResponseDto register(RegisterRequestDto requestDto) {

        // 중복 확인 (이메일, 닉네임)
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userProfileRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호 암호화 (Bcrypt)
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // (⭐️ salt 컬럼 NOT NULL 제약조건을 위한) 랜덤 Salt 생성
        String salt = UUID.randomUUID().toString();

        // User 엔티티 생성
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .salt(salt) // (ERD의 salt 컬럼에 값 저장)
                .build();

        // UserProfile 엔티티 생성
        UserProfile userProfile = UserProfile.builder()
                .username(requestDto.getUsername())
                .build();

        // 연관관계 매핑 (JPA 1:1 관계 설정)
        user.setUserProfile(userProfile);

        // DB에 저장 (User만 저장해도 UserProfile이 @Cascade로 자동 저장됨)
        User savedUser = userRepository.save(user);

        // 응답 DTO로 변환하여 반환
        return RegisterResponseDto.of(savedUser, savedUser.getUserProfile());
    }

    // API 1-2: 로그인은 나중에 여기에 public LoginResponseDto login(...) 메서드로 추가할것입니다.
}