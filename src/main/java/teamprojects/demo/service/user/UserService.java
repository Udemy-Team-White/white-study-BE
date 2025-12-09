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

import teamprojects.demo.service.S3Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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
import teamprojects.demo.dto.user.InventoryListRequest;
import teamprojects.demo.dto.user.InventoryListResponse;
import teamprojects.demo.entity.UserInventory;
import teamprojects.demo.repository.UserInventoryRepository;
import java.time.LocalDateTime;
import java.util.Optional;

import teamprojects.demo.dto.study.StudyInfoDTO;
import teamprojects.demo.dto.study.TodayProgressDTO;
import teamprojects.demo.dto.study.ReportSummaryDTO;
import teamprojects.demo.dto.study.MemberSummaryDTO;
import teamprojects.demo.repository.*;
import teamprojects.demo.dto.user.InventoryStatusUpdateRequest;
import teamprojects.demo.dto.user.InventoryStatusUpdateResponse;
import teamprojects.demo.dto.study.PraiseCreateRequest;
import teamprojects.demo.dto.study.PraiseResponse;
import teamprojects.demo.entity.Praise;
import teamprojects.demo.repository.PraiseRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import teamprojects.demo.dto.user.UserNicknameUpdateRequest;
import teamprojects.demo.dto.user.UserPasswordUpdateRequest;
import java.util.Map;
import teamprojects.demo.entity.StoreItem;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {


    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SelfReportRepository selfReportRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final TodoItemRepository todoItemRepository;
    private final ReliabilityHistoryRepository reliabilityHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointHistoryRepository pointHistoryRepository;
    private final StudyRepository studyRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final S3Service s3Service;
    private final UserInventoryRepository userInventoryRepository;
    private final PraiseRepository praiseRepository;

    /**
     * 회원 가입 로직 (API 1-1)
     */
    @Transactional
    public User signUp(AuthRegisterRequest request) {

        //이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorStatus.EMAIL_ALREADY_EXISTS);
        }
        //닉네임 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(ErrorStatus.USERNAME_ALREADY_EXISTS);
        }

        //Salt 생성 및 비밀번호 암호화
        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //User 엔티티 생성 및 저장
        User newUser = request.toEntity(encodedPassword, salt);
        User savedUser = userRepository.save(newUser);

        //UserProfile 엔티티 자동 생성
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
    @Transactional
    public AuthLoginResponse login(AuthLoginRequest request) {

        //아이디 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorStatus.LOGIN_FAILED));

        //비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorStatus.LOGIN_FAILED); // 401 Bad Request
        }

        // 프로필 조회
        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    // DB에 프로필이 없으면 비상용 프로필을 생성해서 저장합니다.
                    UserProfile newProfile = UserProfile.builder()
                            .user(user)
                            .username(user.getUsername())
                            .points(0)
                            .reliabilityScore(50)
                            .introduction("안녕하세요! 반갑습니당.")
                            .build();
                    return userProfileRepository.save(newProfile);
                });

        Integer requestCount = studyApplicationRepository.countPendingApplicationsByLeaderId(user.getId());

        String rawToken = user.getId() + ":" + user.getEmail();
        String accessToken = java.util.Base64.getEncoder().encodeToString(rawToken.getBytes());

        // 200 OK 응답 데이터 조립
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

        //로그인 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        UserProfile userProfile = userProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 인벤토리 조회 (착용 아이템 추출을 위해 먼저 조회)
        List<UserInventory> userInventories = userInventoryRepository.findAllByUserWithItem(currentUser);

        //착용 중인 아이템의 이미지 URL만 추출 (Stream Filter)
        List<String> equippedItems = userInventories.stream()
                .filter(UserInventory::getEquipped) // 장착된 것만 필터링
                .map(ui -> ui.getStoreItem().getImageUrl())
                .collect(Collectors.toList());

        // Profile Info 조립
        MypageDataResponse.ProfileInfoDto profileInfo = MypageDataResponse.ProfileInfoDto.builder()
                .username(userProfile.getUsername())
                .email(currentUser.getEmail())
                .bio(userProfile.getIntroduction())
                .imgUrl(userProfile.getProfileImageUrl())
                .equippedItems(equippedItems)
                .build();

        //Activity Summary

        // 스터디 수 조회 (RECRUITING_IN_PROGRESS 상태 포함)
        long inProgressCount = studyMemberRepository.countByUserAndStudyStatusIn(
                currentUser,
                List.of(Study.StudyStatus.RECRUITING, Study.StudyStatus.IN_PROGRESS, Study.StudyStatus.RECRUITING_IN_PROGRESS)
        );

        long completedCount = studyMemberRepository.countByUserAndStudyStatusIn(
                currentUser,
                List.of(Study.StudyStatus.FINISHED, Study.StudyStatus.RECRUITMENT_CLOSED)
        );

        // 칭찬 요약
        List<Praise> praises = praiseRepository.findAllByReceiverId(currentUserId);

        Map<String, Long> praiseCountMap = praises.stream()
                .collect(Collectors.groupingBy(Praise::getMessage, Collectors.counting()));

        List<MypageDataResponse.PraiseDto> praiseDtos = praiseCountMap.entrySet().stream()
                .map(entry -> MypageDataResponse.PraiseDto.builder()
                        .praiseType(entry.getKey())
                        .message(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());

        MypageDataResponse.ActivitySummaryDto activitySummary = MypageDataResponse.ActivitySummaryDto.builder()
                .points(userProfile.getPoints())
                .reliabilityScore(userProfile.getReliabilityScore())
                .praise(praiseDtos)
                .inProgressStudies((int) inProgressCount)
                .completedStudies((int) completedCount)
                .build();

        //Inventory Summary DTO 변환
        List<MypageDataResponse.InventorySummaryDto> inventorySummary = userInventories.stream()
                .map(inventory -> {
                    StoreItem item = inventory.getStoreItem();
                    return MypageDataResponse.InventorySummaryDto.builder()
                            .itemId(item.getId())
                            .itemName(item.getItemName())
                            .imageUrl(item.getImageUrl())
                            .equipped(inventory.getEquipped())
                            .build();
                })
                .collect(Collectors.toList());

        // 5. 최종 응답 조립
        return MypageDataResponse.builder()
                .profileInfo(profileInfo)
                .activitySummary(activitySummary)
                .inventorySummary(inventorySummary)
                .build();
    }

    /**
     * API 3-2-1: 닉네임 및 Bio 수정
     */
    @Transactional
    public User updateNickname(Integer userId, UserNicknameUpdateRequest request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 닉네임 변경 로직
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (!user.getUsername().equals(request.getUsername())) {
                if (userRepository.existsByUsername(request.getUsername())) {
                    throw new CustomException(ErrorStatus.USERNAME_ALREADY_EXISTS);
                }

                //User 테이블 변경
                user.updateUsername(request.getUsername());

                //UserProfile 테이블도 같이 변경
                UserProfile profile = userProfileRepository.findByUser(user)
                        .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));
                profile.updateUsername(request.getUsername()); // Entity에 메서드 추가 필요
            }
        }

        // Bio 변경 로직
        if (request.getBio() != null) {
            UserProfile profile = userProfileRepository.findByUser(user)
                    .orElseGet(() -> {
                        UserProfile newProfile = UserProfile.builder()
                                .user(user).username(user.getUsername()).points(0).reliabilityScore(50).build();
                        return userProfileRepository.save(newProfile);
                    });
            profile.updateIntroduction(request.getBio());
        }

        return user;
    }

    /**
     * API 3-2-2: 비밀번호 수정
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
     * API 3-2-3: 프로필 이미지 수정 (S3 업로드)
     */
    @Transactional
    public String updateProfileImage(Integer userId, MultipartFile imageFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .user(user).username(user.getUsername()).points(0).reliabilityScore(50).build();
                    return userProfileRepository.save(newProfile);
                });

        // S3 업로드 및 URL 저장
        String imageUrl = "";
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = s3Service.uploadFile(imageFile); // S3Service 호출
            profile.updateProfileImage(imageUrl); // DB 저장
        }

        return imageUrl;
    }

    /**
     * API 3-3: 내 스터디 목록 조회 로직
     */
    public MyStudiesListResponse getMyStudies(Integer userId, MyStudiesQueryRequest request) {

        // Pageable 생성
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by("createdAt").descending()
        );

        // String -> Enum 변환
        Study.StudyStatus statusEnum = null;
        String reqStatus = request.getStatus();

        if (reqStatus != null && !reqStatus.isBlank()) {
            //ALL 이거나 빈 문자열이면 -> null로 둬서 전체 조회
            if ("ALL".equalsIgnoreCase(reqStatus)) {
                statusEnum = null;
            }
            // 그 외는 Enum으로 변환
            else {
                try {
                    statusEnum = Study.StudyStatus.valueOf(reqStatus);
                } catch (IllegalArgumentException e) {
                    // 오타나 이상한 값이면? -> 일단 전체 조회(null)로 처리하거나 에러
                    statusEnum = null;
                }
            }
        }

        //Repository 호출 (statusEnum이 null이면 전체 조회됨)
        Page<Study> studyPage = studyRepository.findStudiesByUserIdAndStatus(
                userId,
                statusEnum,
                pageable
        );

        // DTO 변환
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
     * API 3-4: 포인트 내역 조회
     */
    @Transactional(readOnly = true)
    public PointHistoryListResponse getPointHistory(Integer userId, PointHistoryQueryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        Integer currentPoints = user.getUserProfile().getPoints();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
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
     * API 3-5: 신뢰도 내역 조회
     */
    @Transactional(readOnly = true)
    public ReliabilityHistoryListResponse getReliabilityHistory(Integer userId, ReliabilityHistoryQueryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        Integer currentScore = user.getUserProfile().getReliabilityScore();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());
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
     * API 4-1: 스터디 대시보드 초기 데이터 조회
     */
    public StudyDashboardResponse getDashboardData(Integer studyId, Integer userId) {

        //User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        //Study 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        //권한 체크 및 내 멤버 정보 가져오기
        // (여기서 에러가 나면 403 -> DB에 멤버 데이터가 없는 것임)
        StudyMember myMemberInfo = studyMemberRepository.findByUserAndStudy(user, study)
                .orElseThrow(() -> new CustomException(ErrorStatus._FORBIDDEN));

        // 다음 스케줄
        List<Schedule> allSchedules = scheduleRepository.findByStudy(study);
        Optional<Schedule> nextScheduleOptional = allSchedules.stream()
                .filter(schedule -> schedule.getScheduleTime().isAfter(LocalTime.now()))
                .findFirst();

        String nextScheduleDateTime = nextScheduleOptional
                .map(Schedule::getScheduleTime)
                .map(time -> LocalDateTime.of(LocalDate.now(), time))
                .map(LocalDateTime::toString)
                .orElse(null);

        // ... (날짜 설정 부분까지 동일)
        LocalDateTime startOfDay = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.now().plusYears(1);

        Long totalItemsLong = todoItemRepository.countTotalItemsForDashboard(studyId, userId, startOfDay, endOfDay);
        Long completedItemsLong = todoItemRepository.countCompletedItemsForDashboard(studyId, userId, startOfDay, endOfDay);

        Integer totalItems = totalItemsLong.intValue();
        Integer completedItems = completedItemsLong.intValue();

        // 실제 개수에 따라 계산되도록 복구
        Integer progressPercentage = (totalItems > 0) ? (int) (((double) completedItems / totalItems) * 100) : 0;

        //최근 보고서
        List<SelfReport> reports = selfReportRepository.findTop3ByStudyOrderByCreatedAtDesc(study);

        // 멤버 목록
        List<StudyMember> members = studyMemberRepository.findByStudy(study);

        //DTO 매핑 및 조합
        StudyInfoDTO studyInfoDTO = StudyInfoDTO.builder()
                .studyId(study.getId())
                .title(study.getTitle())
                .studyName(study.getStudyName())
                .status(study.getStatus().name())
                .startDate(study.getStartDate() != null ? study.getStartDate().toString() : null)
                .endDate(study.getEndDate() != null ? study.getEndDate().toString() : null)
                .myRole(myMemberInfo.getRole().name()) // 내 역할 (LEADER/MEMBER)
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
                .map(member -> {
                    User u = member.getUser();
                    UserProfile profile = userProfileRepository.findByUser(u)
                            .orElse(UserProfile.builder().user(u).build());

                    List<String> equippedItems = new ArrayList<>();

                    return MemberSummaryDTO.builder()
                            .userId(u.getId())
                            .username(u.getUsername())
                            .role(member.getRole().name())
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

    /**
     * API 6-3: 내 인벤토리 조회
     */
    public InventoryListResponse getMyInventory(Integer userId, InventoryListRequest request) {

        //페이징 준비
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("createdAt").descending());

        //필터 처리 (빈 문자열이나 ALL은 null로)
        String filter = request.getFilter();
        if (filter != null && (filter.isBlank() || "ALL".equalsIgnoreCase(filter))) {
            filter = null;
        }

        // DB 조회
        Page<UserInventory> inventoryPage = userInventoryRepository.findByUserAndItemType(userId, filter, pageable);

        // DTO 변환
        List<InventoryListResponse.InventoryItemDto> itemDtos = inventoryPage.getContent().stream()
                .map(ui -> InventoryListResponse.InventoryItemDto.builder()
                        .inventoryId(ui.getId())
                        .itemName(ui.getStoreItem().getItemName())
                        .itemType(ui.getStoreItem().getItemType())
                        .acquiredAt(ui.getCreatedAt().toString())
                        .isEquipped(ui.getEquipped())
                        .imageUrl(ui.getStoreItem().getImageUrl())
                        .build())
                .collect(Collectors.toList());

        //페이지 정보 조립
        InventoryListResponse.PageInfoDto pageInfo = InventoryListResponse.PageInfoDto.builder()
                .page(inventoryPage.getNumber())
                .size(inventoryPage.getSize())
                .totalPages(inventoryPage.getTotalPages())
                .totalElements(inventoryPage.getTotalElements())
                .build();

        return InventoryListResponse.builder()
                .items(itemDtos)
                .pageInfo(pageInfo)
                .build();
    }
    /**
     * API 6-4: 아이템 장착/해제
     */
    @Transactional
    public InventoryStatusUpdateResponse updateInventoryStatus(Integer inventoryId, InventoryStatusUpdateRequest request) {

        //유저 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        //인벤토리 아이템 조회
        UserInventory inventory = userInventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new CustomException(ErrorStatus.ITEM_NOT_FOUND)); // (404 에러 필요 시 추가)

        //권한 체크 (내 아이템인지)
        if (!inventory.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        //장착 시도 시 중복 체크
        if (request.getIsEquipped()) {
            String itemType = inventory.getStoreItem().getItemType();

            // 내가 가진 아이템 중, 같은 타입이고, 이미 장착된 것이 있는지 확인
            boolean isDuplicate = userInventoryRepository.existsByUserAndStoreItem_ItemTypeAndEquippedTrue(
                    inventory.getUser(), itemType
            );

            if (isDuplicate) {
                throw new CustomException(ErrorStatus.ITEM_ALREADY_EQUIPPED);
            }
        }

        // 상태 변경
        inventory.updateEquipped(request.getIsEquipped());

        //응답 반환
        return InventoryStatusUpdateResponse.builder()
                .inventoryId(inventory.getId())
                .isEquipped(inventory.getEquipped())
                .build();
    }

    /**
     * API 6-5: 칭찬 메시지 전송 (보상 지급,횟수 제한)
     */
    @Transactional
    public PraiseResponse sendPraise(Integer studyId, PraiseCreateRequest request) {

        // 나(보내는 사람) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 스터디 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 받는 사람 확인
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 검증 로직

        // 자기 자신 칭찬 불가 (400)
        if (sender.getId().equals(receiver.getId())) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        //권한 체크 (둘 다 스터디 멤버여야 함) (403)
        boolean isSenderMember = studyMemberRepository.existsByUserAndStudy(sender, study);
        boolean isReceiverMember = studyMemberRepository.existsByUserAndStudy(receiver, study);

        if (!isSenderMember || !isReceiverMember) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 칭찬 횟수 제한 체크 (하루 3회)
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay(); // 오늘 00:00:00
        long todayPraiseCount = praiseRepository.countBySenderAndCreatedAtAfter(sender, startOfToday);

        if (todayPraiseCount >= 3) {
            // 칭찬하기는 하루에 3번만 가능합니다.(409 Conflict)
            throw new CustomException(ErrorStatus.PRAISE_LIMIT_EXCEEDED);
        }

        // 칭찬 저장
        Praise praise = Praise.builder()
                .study(study)
                .sender(sender)
                .receiver(receiver)
                .message(request.getMessage())
                .build();

        praise = praiseRepository.save(praise);

        // 보상 지급 (포인트 +10)
        UserProfile senderProfile = userProfileRepository.findByUser(sender)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        int REWARD_POINTS = 10;
        senderProfile.updatePoints(senderProfile.getPoints() + REWARD_POINTS);
        userProfileRepository.save(senderProfile);

        //포인트 내역 기록
        PointHistory history = PointHistory.builder()
                .user(sender)
                .amount(REWARD_POINTS)
                .reason("스터디원 칭찬하기")
                .build();
        pointHistoryRepository.save(history);

        //응답 생성
        return PraiseResponse.builder()
                .praiseId(praise.getId())
                .myRemainingPoints(senderProfile.getPoints())
                .reward(PraiseResponse.RewardDto.builder()
                        .type("POINT")
                        .changeAmount(REWARD_POINTS)
                        .reason("스터디원 칭찬하기")
                        .build())
                .build();
    }
}