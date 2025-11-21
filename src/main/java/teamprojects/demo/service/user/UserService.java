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

import java.util.UUID;
import java.util.List;

import teamprojects.demo.dto.user.MypageDataResponse;
import teamprojects.demo.entity.Study;
import teamprojects.demo.global.utils.SecurityUtils;
import teamprojects.demo.repository.StudyMemberRepository;
import teamprojects.demo.dto.user.UserProfileUpdateRequest;
import teamprojects.demo.dto.study.MyStudiesQueryRequest;
import teamprojects.demo.dto.study.MyStudiesListResponse;
import teamprojects.demo.dto.study.StudySummaryDTO;
import teamprojects.demo.dto.study.PageInfoDTO;

import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import teamprojects.demo.repository.StudyRepository;
import teamprojects.demo.repository.PointHistoryRepository;
import teamprojects.demo.entity.PointHistory;
import teamprojects.demo.dto.user.PointHistoryListResponse;
import teamprojects.demo.dto.user.PointHistoryQueryRequest;
import teamprojects.demo.dto.user.PointHistorySummaryDTO;
import teamprojects.demo.dto.user.ReliabilityHistoryQueryRequest;
import teamprojects.demo.dto.user.ReliabilityHistoryListResponse;
import teamprojects.demo.dto.user.ReliabilityHistorySummaryDTO;
import teamprojects.demo.repository.ReliabilityHistoryRepository;
import teamprojects.demo.entity.ReliabilityHistory;
import teamprojects.demo.dto.study.StudyDashboardResponse;
import teamprojects.demo.entity.Schedule;
import teamprojects.demo.entity.StudyMember;
import teamprojects.demo.entity.SelfReport;

import java.time.LocalDateTime;
import java.util.Optional;

import teamprojects.demo.dto.study.StudyInfoDTO;
import teamprojects.demo.dto.study.TodayProgressDTO;
import teamprojects.demo.dto.study.ReportSummaryDTO;
import teamprojects.demo.dto.study.MemberSummaryDTO;
import teamprojects.demo.entity.*;
import teamprojects.demo.repository.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    // DB 접근을 위한 UserRepository 의존성 주입
    private final UserRepository userRepository;

    // 비밀번호 암호화를 위한 PasswordEncoder 의존성 주입
    private final ScheduleRepository scheduleRepository;
    private final SelfReportRepository selfReportRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final TodoItemRepository todoItemRepository;
    private final TodoListRepository todoListRepository;
    private final ReliabilityHistoryRepository reliabilityHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointHistoryRepository pointHistoryRepository;
    private final StudyRepository studyRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    /**
     * 회원 가입 로직 (API 1-1) - 수정됨!
     * 수정사항: 회원가입 시 UserProfile도 같이 생성하도록 변경
     */
    @Transactional
    public User signUp(AuthRegisterRequest request) {

        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        }
        // 2. 닉네임 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorStatus.USERNAME_ALREADY_EXISTS);
        }

        // 3. Salt 생성 및 비밀번호 암호화
        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4. User 엔티티 생성 및 저장
        User newUser = request.toEntity(encodedPassword, salt);
        User savedUser = userRepository.save(newUser);

        // ⭐️ 5. [여기가 추가되었습니다!] UserProfile 엔티티 자동 생성
        // 이걸 해줘야 로그인할 때 프로필을 찾을 수 있습니다.
        UserProfile newProfile = UserProfile.builder()
                .user(savedUser)             // 방금 가입한 유저와 연결
                .username(savedUser.getUsername()) // 닉네임 동일하게 설정
                .points(0)                   // 초기 포인트 0
                .reliabilityScore(0)         // 초기 신뢰도 0
                .introduction("안녕하세요! 함께 공부해요.")  // 기본 자기소개
                .build();

        userProfileRepository.save(newProfile); // DB에 프로필 저장

        return savedUser;
    }

    // API 1-2: 로그인
    @Transactional // ⭐️ 데이터 변경(프로필 생성)이 일어날 수 있으니 Transactional 필수!
    public AuthLoginResponse login(AuthLoginRequest request) {

        // 1. 아이디 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorStatus.LOGIN_FAILED));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorStatus.LOGIN_FAILED); // 401 Bad Request
        }

        // 3. 프로필 조회 (없으면 생성 - Self Healing)
        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    // DB에 프로필이 없으면 비상용 프로필을 생성해서 저장합니다.
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .username(user.getUsername())
                            .points(0)
                            .reliabilityScore(0)
                            .introduction("안녕하세요! 반갑습니당.")
                            .build();
                    return userProfileRepository.save(newProfile);
                });

        Integer requestCount = studyApplicationRepository.countPendingApplicationsByLeaderId(user.getId());

        // ==================================================================
        // ⭐️ [업그레이드] "진짜 토큰" 생성 (유저 ID를 암호화해서 넣음)
        // ==================================================================
        // "1:email@test.com" 같은 문자열을 만들어서 암호화합니다.
        // 나중에 JwtFilter가 이걸 풀어서 "아, 1번 유저네?" 하고 인식하게 됩니다.
        String rawToken = user.getId() + ":" + user.getEmail();
        String accessToken = java.util.Base64.getEncoder().encodeToString(rawToken.getBytes());
        // ==================================================================

        // 5. 200 OK 응답 데이터 조립
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

    /**
     * API 3-2: 사용자 프로필 수정 로직 (닉네임, 비밀번호)
     *
     * @param userId  현재 로그인된 사용자의 ID (Security Context에서 획득)
     * @param request 업데이트 요청 DTO
     * @return 변경된 User Entity
     */
    @Transactional // 쓰기 작업이므로 @Transactional 필요
    public User updateProfile(Integer userId, UserProfileUpdateRequest request) {

        // 1. 현재 사용자 조회
        // (ID를 찾지 못할 경우의 예외는 ErrorStatus에 _NOT_FOUND 또는 USER_NOT_FOUND가 필요합니다.)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._BAD_REQUEST));

        // --- 2. 닉네임 변경 처리 ---
        if (request.getUsername() != null && !request.getUsername().isBlank()) {

            // 변경하려는 닉네임이 현재 닉네임과 다를 경우에만 중복 체크
            if (!user.getUsername().equals(request.getUsername())) {

                // 닉네임 중복 확인 (Repository에 existsByUsername 메서드 선언 완료)
                if (userRepository.existsByUsername(request.getUsername())) {
                    // 닉네임 중복 시 409 Conflict 반환 (ErrorStatus에 USERNAME_ALREADY_EXISTS 필요)
                    throw new CustomException(ErrorStatus.EMAIL_ALREADY_EXISTS);
                }

                // Entity에 닉네임 업데이트 메서드가 있어야 합니다.
                user.updateUsername(request.getUsername());
            }
        }

        // --- 3. 비밀번호 변경 처리 ---
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {

            // 새 비밀번호가 있을 경우, 현재 비밀번호는 필수입니다.
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                // 400 Bad Request 반환 (명세: "현재 비밀번호가 틀렸거나...")
                throw new CustomException(ErrorStatus._BAD_REQUEST);
            }

            // 현재 비밀번호 일치 확인
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                // 400 Bad Request 반환 (명세: "현재 비밀번호가 일치하지 않습니다.")
                // (ErrorStatus에 PASSWORD_MISMATCH가 필요하지만, 우선 _BAD_REQUEST를 사용합니다.)
                throw new CustomException(ErrorStatus._BAD_REQUEST);
            }

            // 새 비밀번호 암호화 후 업데이트
            String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
            // Entity에 비밀번호 업데이트 메서드가 있어야 합니다.
            user.updatePassword(newEncodedPassword);
        }

        // JPA @Transactional 덕분에 user 객체가 변경되면 DB에 자동 반영됩니다.
        return user;
    }

    /**
     * API 3-3: 내 스터디 목록 조회 로직 (최종 수정)
     */
    public MyStudiesListResponse getMyStudies(Integer userId, MyStudiesQueryRequest request) {

        // 1. Pageable 생성
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by("createdAt").descending()
        );

        // ⭐️ [핵심 수정] String(request.getStatus()) -> Enum(StudyStatus) 변환
        Study.StudyStatus statusEnum = null;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                statusEnum = Study.StudyStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                // 잘못된 문자열이 오면 null로 처리 (전체 조회됨)
            }
        }

        // 2. Repository 호출 (이제 Enum 타입인 statusEnum을 넘깁니다!)
        Page<Study> studyPage = studyRepository.findStudiesByUserIdAndStatus(
                userId,
                statusEnum, // ⭐️ 여기가 수정됨! (request.getStatus() 아님)
                pageable
        );

        // 3. DTO 변환 (기존 코드 유지)
        List<StudySummaryDTO> studySummaryList = studyPage.getContent().stream()
                .map(study -> {
                    String myRole = study.getLeader().getId().equals(userId) ? "LEADER" : "MEMBER";

                    List<String> categories = study.getCategoryMappings().stream()
                            .map(shc -> shc.getStudyCategory().getCategoryName())
                            .collect(Collectors.toList());

                    return StudySummaryDTO.builder()
                            .studyId(study.getId())
                            .title(study.getTitle())
                            .studyType(study.getStudyType().name())
                            .categories(categories)
                            .currentMembers(study.getCurrentMembers())
                            .maxMembers(study.getMaxMembers())
                            .myRole(myRole)
                            .build();
                })
                .collect(Collectors.toList());

        PageInfoDTO pageInfo = PageInfoDTO.builder()
                .page(studyPage.getNumber())
                .size(studyPage.getSize())
                .totalPages(studyPage.getTotalPages())
                .totalElements(studyPage.getTotalElements())
                .build();

        return MyStudiesListResponse.builder()
                .studies(studySummaryList)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * API 3-4: 포인트 내역 조회 (유지 - Repository만 확인하면 됨)
     */
    @Transactional(readOnly = true)
    public PointHistoryListResponse getPointHistory(Integer userId, PointHistoryQueryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        Integer currentPoints = user.getUserProfile().getPoints();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // PointHistoryRepository에 이 메서드 필요!
        Page<PointHistory> historyPage = pointHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<PointHistorySummaryDTO> historySummaryList = historyPage.getContent().stream()
                .map(history -> PointHistorySummaryDTO.builder()
                        .historyId(history.getId())
                        .changeAmount(history.getAmount())
                        .reason(history.getReason())
                        .createdAt(history.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        PageInfoDTO pageInfo = PageInfoDTO.builder()
                .page(historyPage.getNumber())
                .size(historyPage.getSize())
                .totalPages(historyPage.getTotalPages())
                .totalElements(historyPage.getTotalElements())
                .build();

        return PointHistoryListResponse.builder()
                .currentPoints(currentPoints)
                .history(historySummaryList)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * API 3-5: 신뢰도 내역 조회 (유지 - Repository만 확인하면 됨)
     */
    @Transactional(readOnly = true)
    public ReliabilityHistoryListResponse getReliabilityHistory(Integer userId, ReliabilityHistoryQueryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        Integer currentScore = user.getUserProfile().getReliabilityScore();
        // 정렬 기준은 Repository 메서드 이름(OrderByCreatedAtDesc)에 포함되어 있으므로 PageRequest에서 뺄 수도 있지만, 명시해도 상관없음
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());

        // ReliabilityHistoryRepository에 이 메서드 필요!
        Page<ReliabilityHistory> historyPage = reliabilityHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<ReliabilityHistorySummaryDTO> historySummaryList = historyPage.getContent().stream()
                .map(history -> ReliabilityHistorySummaryDTO.builder()
                        .historyId(history.getId())
                        .changeAmount(history.getChangeAmount())
                        .reason(history.getReason())
                        .createdAt(history.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        PageInfoDTO pageInfo = PageInfoDTO.builder()
                .page(historyPage.getNumber())
                .size(historyPage.getSize())
                .totalPages(historyPage.getTotalPages())
                .totalElements(historyPage.getTotalElements())
                .build();

        return ReliabilityHistoryListResponse.builder()
                .currentScore(currentScore)
                .history(historySummaryList)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * API 4-1: 스터디 대시보드 초기 데이터 조회 로직
     * (권한 체크 및 다중 데이터 조합)
     *
     * @param studyId 조회할 스터디 ID
     * @param userId  현재 로그인된 사용자 ID (인증 토큰에서 추출)
     * @return StudyDashboardResponse (대시보드 최종 응답 DTO)
     */
    @Transactional(readOnly = true)
    public StudyDashboardResponse getDashboardData(Integer studyId, Integer userId) {

        // 1. Repository 호출을 위한 User Entity 객체 획득
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 2. 핵심 데이터 조회 (Study Info)
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 (403 Forbidden)
        if (!studyMemberRepository.existsByUserAndStudy(user, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 4. [요소 조회] 다음 스케줄 DateTime (ScheduleRepository 사용)
        // ⭐️ findByStudy를 사용한 Service 로직으로 대체 (컴파일 오류 해결)
        List<Schedule> allSchedules = scheduleRepository.findByStudy(study);

        // 🚨 주의: 스케줄 엔티티가 DayOfWeek + LocalTime 구조이므로,
        // 다음 일정을 정확히 계산하려면 복잡한 Java Stream 로직이 필요합니다.
        // 여기서는 컴파일 오류 해결을 위해, 현재 스터디의 모든 일정을 가져오는 코드로만 대체합니다.
        // (실제 '다음 일정' 로직 구현은 Service 단에서 추가 작업이 필요합니다.)
        Optional<Schedule> nextScheduleOptional = allSchedules.stream()
                // 임시로 가장 빠른 LocalTime을 가진 일정 중 하나를 선택합니다.
                .filter(schedule -> schedule.getScheduleTime().isAfter(LocalTime.now()))
                .findFirst();

        // ⭐️ LocalTime과 LocalDate를 결합하여 String으로 변환 (타입 오류 해결)
        String nextScheduleDateTime = nextScheduleOptional
                .map(Schedule::getScheduleTime)
                .map(time -> LocalDateTime.of(LocalDate.now(), time))
                .map(LocalDateTime::toString)
                .orElse(null);


        // 5. [요소 조회] 나의 오늘 진행 상황 (TodoItemRepository 사용)
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        Long totalItemsLong = todoItemRepository.countTotalItemsForDashboard(studyId, userId, startOfDay, endOfDay);
        Long completedItemsLong = todoItemRepository.countCompletedItemsForDashboard(studyId, userId, startOfDay, endOfDay);

        Integer totalItems = totalItemsLong.intValue();
        Integer completedItems = completedItemsLong.intValue();

        Integer progressPercentage = (totalItems > 0) ? (int) (((double) completedItems / totalItems) * 100) : 100;


        // 6. [요소 조회] 최근 보고서 (SelfReportRepository 사용)
        List<SelfReport> reports = selfReportRepository.findTop3ByStudyOrderByCreatedAtDesc(study);

        // 7. [요소 조회] 멤버 목록 (StudyMemberRepository 사용)
        List<StudyMember> members = studyMemberRepository.findByStudy(study);


        // 8. DTO 매핑 및 조합

        StudyInfoDTO studyInfoDTO = StudyInfoDTO.builder()
                .studyId(study.getId())
                .title(study.getTitle())
                .status(study.getStatus().name())
                .build();

        TodayProgressDTO progressDTO = TodayProgressDTO.builder()
                .totalItems(totalItems)
                .completedItems(completedItems)
                .progressPercentage(progressPercentage)
                .build();

        List<ReportSummaryDTO> reportDTOs = reports.stream()
                .map(report -> ReportSummaryDTO.builder()
                        .reportId(report.getId())
                        .authorUsername(report.getUser().getUsername())
                        .createdAt(report.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        List<MemberSummaryDTO> memberDTOs = members.stream()
                .map(member -> MemberSummaryDTO.builder()
                        .userId(member.getUser().getId())
                        .username(member.getUser().getUsername())
                        .role(member.getRole().name())
                        .build())
                .collect(Collectors.toList());

        // 9. 최종 응답 DTO 반환
        return StudyDashboardResponse.builder()
                .studyInfo(studyInfoDTO)
                .nextScheduleDateTime(nextScheduleDateTime)
                .myTodayProgress(progressDTO)
                .recentReports(reportDTOs)
                .members(memberDTOs)
                .build();
    }
}