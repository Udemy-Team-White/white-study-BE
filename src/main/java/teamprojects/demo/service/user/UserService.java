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

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

import teamprojects.demo.dto.user.MypageDataResponse;
import teamprojects.demo.entity.Study;
import teamprojects.demo.global.utils.SecurityUtils;
import teamprojects.demo.repository.StudyMemberRepository;
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
import teamprojects.demo.repository.*;

import java.time.LocalDate;
import java.time.LocalTime;

import teamprojects.demo.dto.user.UserNicknameUpdateRequest;
import teamprojects.demo.dto.user.UserPasswordUpdateRequest;

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
                .reliabilityScore(50)         // 초기 신뢰도 50
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

        // [추가] 착용 아이템 목록 가져오기 (아직 구현 전이므로 빈 리스트 처리)
        // 나중에 UserInventoryRepository가 생기면 여기서 조회해서 넣으면 됩니다.
        List<String> equippedItems = new ArrayList<>();

        // 2. Profile Info
        MypageDataResponse.ProfileInfoDto profileInfo = MypageDataResponse.ProfileInfoDto.builder()
                .username(userProfile.getUsername())
                .email(currentUser.getEmail()) // User 엔티티에 email 필드가 있다고 가정
                .bio(userProfile.getIntroduction())
                .imgUrl(userProfile.getProfileImageUrl())
                .equippedItems(equippedItems)
                .build();

        // 3. Activity Summary

        // 3-1. 진행 중/완료 스터디 수 조회
        //  StudyStatus Enum은 Study 엔티티에 있다고 가정합니다.
        long inProgressCount = studyMemberRepository.countByUserAndStudyStatusIn(
                currentUser,
                List.of(Study.StudyStatus.RECRUITING, Study.StudyStatus.IN_PROGRESS)
        );

        long completedCount = studyMemberRepository.countByUserAndStudyStatusIn(
                currentUser,
                List.of(Study.StudyStatus.FINISHED, Study.StudyStatus.RECRUITMENT_CLOSED)
        );

        // 3-2. 칭찬 요약 (PraiseRepository를 사용한다고 가정합니다.)
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
     * API 3-2-1: 닉네임 및 Bio 수정 (기능 확장됨)
     */
    @Transactional
    public User updateNickname(Integer userId, UserNicknameUpdateRequest request) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 2. 닉네임 변경 로직 (기존 유지)
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!user.getUsername().equals(request.getUsername())) {
                if (userRepository.existsByUsername(request.getUsername())) {
                    throw new CustomException(ErrorStatus.USERNAME_ALREADY_EXISTS);
                }
                user.updateUsername(request.getUsername());
            }
        }

        // 3. ⭐️ [추가됨] Bio(자기소개) 변경 로직
        // UserProfile을 찾아서 업데이트합니다.
        if (request.getBio() != null) { // bio 값이 들어왔을 때만 수정
            UserProfile profile = userProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        // 혹시 프로필이 없으면 새로 만듦 (안전장치)
                        UserProfile newProfile = UserProfile.builder()
                                .user(user)
                                .username(user.getUsername())
                                .points(0)
                                .reliabilityScore(50)
                                .build();
                        return userProfileRepository.save(newProfile);
                    });

            // 엔티티의 introduction 필드 업데이트
            profile.updateIntroduction(request.getBio());
        }

        return user;
    }

    /**
     * API 3-2-2: 비밀번호 수정 (분리됨)
     */
    @Transactional
    public void updatePassword(Integer userId, UserPasswordUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 400 Bad Request: 현재 비밀번호 불일치
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorStatus.PASSWORD_MISMATCH);
        }

        // 새 비밀번호 암호화 및 변경
        String newEncodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(newEncodedPassword);
    }
    /**
     * API 3-3: 내 스터디 목록 조회 로직 (최종 수정: ALL 필터 추가)
     */
    public MyStudiesListResponse getMyStudies(Integer userId, MyStudiesQueryRequest request) {

        // 1. Pageable 생성
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by("createdAt").descending()
        );

        // [수정] String -> Enum 변환 (ALL 처리 추가)
        Study.StudyStatus statusEnum = null;
        String reqStatus = request.getStatus();

        if (reqStatus != null && !reqStatus.isBlank()) {
            // 1) "ALL" 이거나 빈 문자열이면 -> null로 둬서 전체 조회
            if ("ALL".equalsIgnoreCase(reqStatus)) {
                statusEnum = null;
            }
            // 2) 그 외(IN_PROGRESS 등)는 Enum으로 변환
            else {
                try {
                    statusEnum = Study.StudyStatus.valueOf(reqStatus);
                } catch (IllegalArgumentException e) {
                    // 오타나 이상한 값이면? -> 일단 전체 조회(null)로 처리하거나 에러 (여기선 편의상 전체 조회)
                    statusEnum = null;
                }
            }
        }

        // 2. Repository 호출 (statusEnum이 null이면 전체 조회됨)
        Page<Study> studyPage = studyRepository.findStudiesByUserIdAndStatus(
                userId,
                statusEnum,
                pageable
        );

        // 3. DTO 변환
        List<StudySummaryDTO> studySummaryList = studyPage.getContent().stream()
                .map(study -> {
                    // 역할 결정
                    String myRole = study.getLeader().getId().equals(userId) ? "LEADER" : "MEMBER";

                    // 카테고리 매핑
                    List<String> categories = study.getCategoryMappings().stream()
                            .map(shc -> shc.getStudyCategory().getCategoryName())
                            .collect(Collectors.toList());

                    return StudySummaryDTO.builder()
                            .studyId(study.getId())
                            .title(study.getTitle())
                            .studyName(study.getStudyName())
                            .studyType(study.getStudyType().name())
                            .categories(categories)
                            .currentMembers(study.getCurrentMembers())
                            .maxMembers(study.getMaxMembers())
                            .myRole(myRole)
                            .status(study.getStatus().name())
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
     * API 4-1: 스터디 대시보드 초기 데이터 조회 로직 (최종 수정: 멤버 프로필 정보 추가)
     */
    @Transactional(readOnly = true)
    public StudyDashboardResponse getDashboardData(Integer studyId, Integer userId) {

        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 2. Study 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 및 내 멤버 정보 가져오기
        StudyMember myMemberInfo = studyMemberRepository.findByUserAndStudy(user, study)
                .orElseThrow(() -> new CustomException(ErrorStatus._FORBIDDEN));

        // 4. [요소 조회] 다음 스케줄
        List<Schedule> allSchedules = scheduleRepository.findByStudy(study);
        Optional<Schedule> nextScheduleOptional = allSchedules.stream()
                .filter(schedule -> schedule.getScheduleTime().isAfter(LocalTime.now()))
                .findFirst();

        String nextScheduleDateTime = nextScheduleOptional
                .map(Schedule::getScheduleTime)
                .map(time -> LocalDateTime.of(LocalDate.now(), time))
                .map(LocalDateTime::toString)
                .orElse(null);

        // 5. [요소 조회] 나의 오늘 진행 상황
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        Long totalItemsLong = todoItemRepository.countTotalItemsForDashboard(studyId, userId, startOfDay, endOfDay);
        Long completedItemsLong = todoItemRepository.countCompletedItemsForDashboard(studyId, userId, startOfDay, endOfDay);

        Integer totalItems = totalItemsLong.intValue();
        Integer completedItems = completedItemsLong.intValue();
        Integer progressPercentage = (totalItems > 0) ? (int) (((double) completedItems / totalItems) * 100) : 100;

        // 6. [요소 조회] 최근 보고서
        List<SelfReport> reports = selfReportRepository.findTop3ByStudyOrderByCreatedAtDesc(study);

        // 7. [요소 조회] 멤버 목록
        List<StudyMember> members = studyMemberRepository.findByStudy(study);

        // 8. DTO 매핑 및 조합
        StudyInfoDTO studyInfoDTO = StudyInfoDTO.builder()
                .studyId(study.getId())
                .title(study.getTitle())
                .studyName(study.getStudyName())
                .status(study.getStatus().name())
                .startDate(study.getStartDate() != null ? study.getStartDate().toString() : null)
                .endDate(study.getEndDate() != null ? study.getEndDate().toString() : null)
                .myRole(myMemberInfo.getRole().name())
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

        // ⭐️ [수정됨] 멤버 목록 매핑 시 프로필 정보(이미지, 아이템) 추가
        List<MemberSummaryDTO> memberDTOs = members.stream()
                .map(member -> {
                    User u = member.getUser();

                    // 프로필 조회 (없으면 빌더로 빈 객체 생성)
                    UserProfile profile = userProfileRepository.findByUser(u)
                            .orElse(UserProfile.builder().user(u).build());

                    // 아이템 조회 (임시 빈 리스트 - 나중에 구현)
                    List<String> equippedItems = new ArrayList<>();

                    return MemberSummaryDTO.builder()
                            .userId(u.getId())
                            .username(u.getUsername())
                            .role(member.getRole().name())
                            // ⭐️ 추가된 필드 매핑
                            .imgUrl(profile.getProfileImageUrl())
                            .equippedItems(equippedItems)
                            .build();
                })
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