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
import teamprojects.demo.dto.auth.AuthCheckEmailResponse;
import java.util.UUID; //Salt 생성을 위해 UUID import
import java.util.List;
import teamprojects.demo.dto.user.MypageDataResponse; // ⭐️ 에러 3 해결
import teamprojects.demo.entity.Study; // ⭐️ 에러 6 해결
import teamprojects.demo.global.utils.SecurityUtils; // ⭐️ 에러 4 해결
import teamprojects.demo.repository.StudyMemberRepository;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    // DB 접근을 위한 UserRepository 의존성 주입
    private final UserRepository userRepository;

    // 비밀번호 암호화를 위한 PasswordEncoder 의존성 주입
    private final PasswordEncoder passwordEncoder;

    private final StudyMemberRepository studyMemberRepository;
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
    //API 1-2
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
    //API 1-3
    public AuthCheckEmailResponse checkEmailAvailability(String email) {
        boolean isUsed = userRepository.existsByEmail(email);

        // API 명세서에 따라, 중복 여부(isUsed)를 반전시켜 '사용 가능 여부(!isUsed)'를 반환합니다.
        // (409 에러 처리는 Controller에서 isAvailable이 false일 때 별도로 처리할 수 있습니다.)
        return AuthCheckEmailResponse.builder()
                .isAvailable(!isUsed)
                .build();
    }
    //API 1-4
    public AuthCheckEmailResponse checkUsernameAvailability(String username) {
        boolean isUsed = userRepository.existsByUsername(username);

        return AuthCheckEmailResponse.builder()
                .isAvailable(!isUsed)
                .build();
    }

    /**
     * API 3-1: 마이페이지 초기 데이터 조회
     */
    public MypageDataResponse getUserMypageData() {

        // 1. 로그인 사용자 확인 (401 Unauthorized)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        UserProfile userProfile = userProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. Profile Info
        MypageDataResponse.ProfileInfoDto profileInfo = MypageDataResponse.ProfileInfoDto.builder()
                .username(userProfile.getUsername())
                .email(currentUser.getEmail()) // User 엔티티에 email 필드가 있다고 가정
                .bio(userProfile.getIntroduction())
                .imgUrl(userProfile.getProfileImageUrl())
                .build();

        // 3. Activity Summary

        // 3-1. 진행 중/완료 스터디 수 조회
        // ⭐️ [필수] StudyStatus Enum은 Study 엔티티에 있다고 가정합니다.
        long inProgressCount = studyMemberRepository.countByUserAndStudyStatusIn(
                currentUser,
                List.of(Study.StudyStatus.RECRUITING, Study.StudyStatus.IN_PROGRESS)
        );

        long completedCount = studyMemberRepository.countByUserAndStudyStatusIn(
                currentUser,
                List.of(Study.StudyStatus.FINISHED, Study.StudyStatus.RECRUITMENT_CLOSED)
        );

        // 3-2. 칭찬 요약 (⭐️ [가정] PraiseRepository를 사용한다고 가정합니다.)
        // 실제 구현 시에는 PraiseRepository에 사용자 정의 쿼리(GROUP BY message)가 필요합니다.
        List<MypageDataResponse.PraiseDto> praiseDtos = List.of(); // 임시로 빈 목록 반환
        /*
        List<MypageDataResponse.PraiseDto> praiseDtos = praiseRepository.findTopPraiseSummaryByUser(currentUser).stream()
                .map(praiseResult -> MypageDataResponse.PraiseDto.builder()
                        .message((String) praiseResult[0])
                        .count(((Long) praiseResult[1]).intValue())
                        .build())
                .collect(Collectors.toList());
        */

        MypageDataResponse.ActivitySummaryDto activitySummary = MypageDataResponse.ActivitySummaryDto.builder()
                .points(userProfile.getPoints())
                .reliabilityScore(userProfile.getReliabilityScore())
                .praise(praiseDtos)
                .inProgressStudies((int) inProgressCount)
                .completedStudies((int) completedCount)
                .build();

        // 4. Inventory Summary (⭐️ [가정] UserInventoryRepository를 사용한다고 가정합니다.)
        // 실제 구현 시에는 UserInventory 엔티티에 Item 엔티티가 연결되어 있어야 합니다.
        List<MypageDataResponse.InventorySummaryDto> inventorySummary = List.of(); // 임시로 빈 목록 반환
        /*
        List<MypageDataResponse.InventorySummaryDto> inventorySummary = userInventoryRepository.findByUser(currentUser).stream()
                .map(userInventory -> MypageDataResponse.InventorySummaryDto.builder()
                        .itemId(userInventory.getItem().getId())
                        .itemName(userInventory.getItem().getName())
                        .equipped(userInventory.isEquipped()) // isEquipped 필드 가정
                        .build())
                .collect(Collectors.toList());
        */

        // 5. 최종 응답 조립
        return MypageDataResponse.builder()
                .profileInfo(profileInfo)
                .activitySummary(activitySummary)
                .inventorySummary(inventorySummary)
                .build();
    }
}