package teamprojects.demo.service.study;

import teamprojects.demo.dto.category.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamprojects.demo.dto.study.StudyCreateRequest;
import teamprojects.demo.dto.study.StudyCreateResponse;
import teamprojects.demo.dto.study.StudyListRequest;
import teamprojects.demo.dto.study.StudyListResponse;
import org.springframework.data.domain.Sort;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.StudyCategory;
import teamprojects.demo.entity.StudyHasCategory;
import teamprojects.demo.entity.StudyMember;
import teamprojects.demo.entity.User;
import teamprojects.demo.entity.TodoList;
import teamprojects.demo.global.common.code.status.ErrorStatus;
import teamprojects.demo.global.common.exception.CustomException;
import teamprojects.demo.global.utils.SecurityUtils;
import teamprojects.demo.repository.*;
import teamprojects.demo.entity.UserProfile;
import teamprojects.demo.dto.study.StudyDetailResponse;
import teamprojects.demo.dto.study.StudyApplyRequest;
import teamprojects.demo.dto.study.StudyApplyResponse;
import teamprojects.demo.entity.StudyApplication;
import teamprojects.demo.dto.study.TodoPlannerResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import teamprojects.demo.dto.study.TodoListCreateRequest;
import teamprojects.demo.dto.study.TodoListCreateResponse;
import teamprojects.demo.repository.TodoListRepository;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import teamprojects.demo.dto.study.TodoItemCreateRequest;
import teamprojects.demo.dto.study.TodoItemResponse;
import teamprojects.demo.entity.TodoItem;
import teamprojects.demo.repository.TodoItemRepository;
import teamprojects.demo.dto.study.TodoItemUpdateRequest;
import teamprojects.demo.dto.study.TodoItemStatusResponse;
import teamprojects.demo.dto.study.TodoItemDeleteResponse;
import teamprojects.demo.global.utils.HtmlStripper;
import teamprojects.demo.entity.SelfReport;
import teamprojects.demo.entity.ReliabilityHistory;
import teamprojects.demo.dto.study.*;
import teamprojects.demo.entity.*;
import teamprojects.demo.repository.*;




@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyHasCategoryRepository studyHasCategoryRepository;
    private final UserRepository userRepository;
    private final StudyCategoryRepository studyCategoryRepository;
    private final UserProfileRepository userProfileRepository; // ⭐️ 스터디장 신뢰도 조회용
    private final StudyApplicationRepository studyApplicationRepository; // ⭐️ 신청 상태 조회용
    private final TodoListRepository todoListRepository;
    private final TodoItemRepository todoItemRepository;
    private final SelfReportRepository selfReportRepository;
    private final ReliabilityHistoryRepository reliabilityHistoryRepository;


    public List<CategoryResponse> getAllCategories() {
        return studyCategoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * API 1-5: 스터디 목록 조회 (최종 수정: 정렬, 검색, 작성일, Null 방어 적용)
     */
    public StudyListResponse getStudyList(StudyListRequest request) {

        // 1. 정렬 기준 설정
        Sort sort;
        if ("members".equals(request.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "maxMembers");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        // 2. 페이지네이션 생성
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 3. 검색 조건 생성 (Specification)
        Specification<Study> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (콤마로 구분)
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String[] keywords = request.getKeyword().split(",");
                List<Predicate> keywordPredicates = new ArrayList<>();

                for (String keyword : keywords) {
                    String trimKeyword = keyword.trim();
                    Predicate titleLike = criteriaBuilder.like(root.get("title"), "%" + trimKeyword + "%");
                    Predicate contentLike = criteriaBuilder.like(root.get("content"), "%" + trimKeyword + "%");

                    // ⭐️ [수정] 중복된 add 코드를 하나로 정리했습니다.
                    keywordPredicates.add(criteriaBuilder.or(titleLike, contentLike));
                }
                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 4. DB 조회
        Page<Study> studyPage = studyRepository.findAll(spec, pageable);

        // 5. DTO 변환
        List<StudyListResponse.StudyDto> studyDtos = studyPage.getContent().stream()
                .map(study -> {
                    Integer currentMembers = studyMemberRepository.countByStudy(study);
                    List<String> categories = studyHasCategoryRepository.findByStudy(study).stream()
                            .map(shc -> shc.getStudyCategory().getCategoryName())
                            .collect(Collectors.toList());

                    return StudyListResponse.StudyDto.builder()
                            .studyId(study.getId())
                            .title(study.getTitle())
                            .studyName(study.getStudyName())
                            .studyType(study.getStudyType().name())
                            .categories(categories)
                            .currentMembers(currentMembers)
                            .maxMembers(study.getMaxMembers())

                            // ⭐️ [수정] closedAt이 Null일 경우 방어 로직 추가
                            .closedAt(study.getClosedAt() != null ? study.getClosedAt().toString() : null)

                            .status(study.getStatus().name())
                            .createdAt(study.getCreatedAt().toString())
                            .build();
                })
                .collect(Collectors.toList());

        // 6. 페이지 정보 조립
        StudyListResponse.PageInfoDto pageInfo = StudyListResponse.PageInfoDto.builder()
                .page(studyPage.getNumber())
                .size(studyPage.getSize())
                .totalPages(studyPage.getTotalPages())
                .totalElements(studyPage.getTotalElements())
                .build();

        return StudyListResponse.builder()
                .studies(studyDtos)
                .pageInfo(pageInfo)
                .build();
    }

    /**
     * API 2-1: 스터디 개설 (최종 완성본)
     */
    @Transactional
    public StudyCreateResponse createStudy(StudyCreateRequest request) {

        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User leader = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // [Null 방어 1] studyType이 없으면 'ONLINE'으로 기본 설정
        Study.StudyType type;
        if (request.getStudyType() == null || request.getStudyType().isBlank()) {
            type = Study.StudyType.ONLINE; // 기본값
        } else {
            try {
                type = Study.StudyType.valueOf(request.getStudyType());
            } catch (IllegalArgumentException e) {
                type = Study.StudyType.ONLINE; // 이상한 값 들어오면 기본값
            }
        }

        // 2. 스터디 생성
        Study newStudy = Study.builder()
                .title(request.getTitle())
                .studyName(request.getTitle())
                .content(request.getContent())
                .studyType(type)
                .maxMembers(request.getMaxMembers())
                .closedAt(request.getClosedAt())
                .startDate(request.getStartDate())
                .status(Study.StudyStatus.RECRUITING)
                .leader(leader)
                .createdAt(LocalDateTime.now())
                .build();

        newStudy = studyRepository.save(newStudy);

        // 3. 카테고리 연결 (Null 방어)
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Integer categoryId : request.getCategoryIds()) {
                StudyCategory category = studyCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new CustomException(ErrorStatus.CATEGORY_NOT_FOUND));

                StudyHasCategory link = StudyHasCategory.builder()
                        .study(newStudy)
                        .studyCategory(category)
                        .build();

                studyHasCategoryRepository.save(link);
            }
        }

        // 4. 스터디장 멤버 등록
        StudyMember leaderMember = StudyMember.builder()
                .study(newStudy)
                .user(leader)
                .role(StudyMember.StudyRole.LEADER)
                .build();

        studyMemberRepository.save(leaderMember);

        // 5. 응답 반환
        return StudyCreateResponse.builder()
                .studyId(newStudy.getId())
                .build();
    }
    /**
     * API 2-2: 스터디 상세 정보 조회
     * (로그인 여부와 관계없이 조회 가능, 접속자 상태 userStatus 반환)
     */
    public StudyDetailResponse getStudyDetail(Integer studyId) {

        // 1. 스터디 조회 (없으면 404)
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 2. 카테고리 목록 조회
        List<StudyDetailResponse.CategoryDto> categoryDtos = studyHasCategoryRepository.findByStudy(study).stream()
                .map(shc -> StudyDetailResponse.CategoryDto.builder()
                        .categoryId(shc.getStudyCategory().getId())
                        .name(shc.getStudyCategory().getCategoryName())
                        .build())
                .collect(Collectors.toList());

        // 3. 스터디장 정보 조회 (UserProfile에서 신뢰도 가져오기)
        User leader = study.getLeader();
        UserProfile leaderProfile = userProfileRepository.findByUser(leader)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        StudyDetailResponse.StudyLeaderDto leaderDto = StudyDetailResponse.StudyLeaderDto.builder()
                .username(leader.getUsername())
                .reliabilityScore(leaderProfile.getReliabilityScore())
                .build();

        // 4. 현재 접속자의 상태(userStatus) 판별
        String userStatus = determineUserStatus(study);

        // 5. 현재 멤버 수 조회
        Integer currentMembers = studyMemberRepository.countByStudy(study);

        // 6. DTO 조립 및 반환
        return StudyDetailResponse.builder()
                .studyInfo(StudyDetailResponse.StudyInfoDto.builder()
                        .studyId(study.getId())
                        .title(study.getTitle())
                        .content(study.getContent())
                        .studyType(study.getStudyType().name())
                        .status(study.getStatus().name())
                        .currentMembers(currentMembers)
                        .maxMembers(study.getMaxMembers())
                        .closedAt(study.getClosedAt().toString())
                        .startDate(study.getStartDate() != null ? study.getStartDate().toString() : null)
                        .build())
                .categories(categoryDtos)
                .studyLeader(leaderDto)
                .userStatus(userStatus)
                .build();
    }

    // ⭐️ 내부 헬퍼 메서드: 유저 상태 판별 로직
    private String determineUserStatus(Study study) {
        // 1. 비로그인 사용자 (GUEST)
        Integer currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return "GUEST";
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 스터디장 (LEADER)
        if (study.getLeader().getId().equals(currentUserId)) {
            return "LEADER";
        }

        // 3. 이미 참여 중인 멤버 (MEMBER)
        if (studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            return "MEMBER";
        }

        // 4. ⭐️ [수정됨] 신청 후 승인 대기 중 (APPLIED) - Enum 타입으로 안전하게 비교!
        boolean isPending = studyApplicationRepository.findByUserAndStudy(currentUser, study)
                .stream()
                // Enum 값(PENDING)과 직접 비교합니다. (가장 확실한 방법)
                .anyMatch(app -> app.getStatus() == StudyApplication.ApplicationStatus.PENDING);

        if (isPending) {
            return "APPLIED";
        }

        // 5. 아무 관계 없는 로그인 사용자 (NONE)
        return "NONE";
    }

    /**
     * API 2-3: 스터디 참여 신청 (최종 수정: 상태 추가 & Enum 안전 비교)
     */
    @Transactional
    public StudyApplyResponse applyToStudy(Integer studyId, StudyApplyRequest request) {

        // 1. 유저 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User applicant = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 스터디 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // ⭐️ [수정 1] 모집 상태 확인 (조건 추가!)
        // "모집 중" 이거나 "진행 중&추가모집" 상태면 신청 가능!
        boolean isRecruiting = study.getStatus() == Study.StudyStatus.RECRUITING ||
                study.getStatus() == Study.StudyStatus.RECRUITING_IN_PROGRESS;

        if (!isRecruiting) {
            throw new CustomException(ErrorStatus.RECRUITMENT_CLOSED);
        }

        // 3. 중복 확인

        // 3-1. 이미 멤버인지 확인
        if (studyMemberRepository.existsByUserAndStudy(applicant, study)) {
            throw new CustomException(ErrorStatus.ALREADY_MEMBER_OR_APPLIED);
        }

        // 3-2. ⭐️ [수정 2] 이미 신청했는지 확인 (Enum 안전 비교)
        // 리포지토리 메서드(exists...String) 대신, 리스트를 가져와서 스트림으로 검사합니다.
        // (이게 가장 안전하고 확실한 방법입니다.)
        boolean alreadyApplied = studyApplicationRepository.findByUserAndStudy(applicant, study)
                .stream()
                .anyMatch(app -> app.getStatus() == StudyApplication.ApplicationStatus.PENDING);

        if (alreadyApplied) {
            throw new CustomException(ErrorStatus.ALREADY_MEMBER_OR_APPLIED);
        }

        // 4. 저장
        StudyApplication newApplication = StudyApplication.builder()
                .study(study)
                .user(applicant)
                .status(StudyApplication.ApplicationStatus.PENDING)
                .message(request.getMessage())
                .build();

        newApplication = studyApplicationRepository.save(newApplication);

        return StudyApplyResponse.builder()
                .applicationId(newApplication.getId())
                .status(newApplication.getStatus().name())
                .build();
    }
    /**
     * API 3-2: TODO 플래너 조회 (날짜별)
     * 님의 TodoListRepository 메서드(Between)에 맞춰 수정되었습니다.
     */
    @Transactional(readOnly = true)
    public List<TodoPlannerResponse> getStudyTodos(Integer studyId, LocalDate targetDate) {

        // 1. 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 스터디 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 멤버 자격 확인
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus.STUDY_NOT_FOUND); // 또는 403 Forbidden
        }

        // 4. ⭐️ [수정됨] 날짜 범위 계산 (LocalDate -> LocalDateTime Start/End)
        // 예: 2025-11-15 -> 2025-11-15 00:00:00 ~ 2025-11-15 23:59:59
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(23, 59, 59);

        // 5. ⭐️ [수정됨] Repository 호출 (파라미터 순서: User, Study, Start, End)
        List<TodoList> todoLists = todoListRepository.findByUserAndStudyAndTargetDateBetween(
                currentUser, study, startOfDay, endOfDay);

        // 6. 응답 DTO 변환
        return todoLists.stream()
                .map(todoList -> {
                    // ⭐️ [수정] getItems() -> getTodoItems() 로 변경!
                    List<TodoPlannerResponse.TodoItemDto> itemDtos = todoList.getTodoItems().stream()
                            .map(todoItem -> TodoPlannerResponse.TodoItemDto.builder()
                                    .todoItemId(todoItem.getId())
                                    .content(todoItem.getContent())
                                    .isCompleted(todoItem.getIsCompleted()) // (참고: TodoItem 엔티티 필드명 확인 필요)
                                    .build())
                            .collect(Collectors.toList());

                    return TodoPlannerResponse.builder()
                            .todoListId(todoList.getId())
                            .title(todoList.getTitle())
                            .items(itemDtos)
                            .build();
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public TodoListCreateResponse createTodoList(Integer studyId, TodoListCreateRequest request) {

        // 1. 현재 로그인 사용자 확인 (401 Unauthorized)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 스터디 존재 확인 (404)
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 스터디 멤버 자격 확인 (404/403 - 멤버만 TODO를 생성할 수 있음)
        // ⭐️ StudyMemberRepository에 existsByUserAndStudy 메서드가 있어야 합니다.
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            // 멤버가 아니면 404를 반환하여 권한 없음/정보 없음으로 처리합니다.
            throw new CustomException(ErrorStatus.STUDY_NOT_FOUND);
        }

        // 4. TodoList Entity 생성 및 저장
        TodoList newTodoList = TodoList.builder()
                .study(study)
                .user(currentUser)
                .title(request.getTitle()) // Optional 제목
                .targetDate(request.getTargetDate()) // 필수 날짜
                .build();

        newTodoList = todoListRepository.save(newTodoList);

        // 5. 응답 DTO 반환 (빈 items 리스트와 함께 반환)
        return TodoListCreateResponse.builder()
                .todoListId(newTodoList.getId())
                .title(newTodoList.getTitle())
                .targetDate(newTodoList.getTargetDate())
                .items(new ArrayList<>()) // 생성 시에는 항상 빈 목록 반환
                .build();
    }
    /**
     * API 4-4: TODO 항목 생성 로직 (Service)
     * listId: Path Variable로 받은 TodoList ID
     */
    @Transactional
    public TodoItemResponse createTodoItem(Integer listId, TodoItemCreateRequest request) {

        // 1. 현재 사용자 확인 (401 Unauthorized 방지)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. TodoList 존재 확인 (404 Not Found)
        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 3. 권한 확인 (403 Forbidden)
        // TodoList의 소유자(User)와 현재 로그인한 유저의 ID가 일치하는지 확인
        if (!todoList.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // ⭐️ 403 Forbidden 반환
        }

        // 4. TodoItem Entity 생성
        TodoItem newItem = TodoItem.builder()
                .todoList(todoList)
                .content(request.getContent())
                .isCompleted(false) // 명세대로 항상 false로 생성
                .build();

        newItem = todoItemRepository.save(newItem);

        // 5. 응답 DTO 반환
        return TodoItemResponse.builder()
                .todoItemId(newItem.getId())
                .content(newItem.getContent())
                .isCompleted(newItem.getIsCompleted())
                .build();
    }
    /**
     * API 4-5: TODO 항목 상태 변경 (Service)
     */
    @Transactional
    public TodoItemStatusResponse updateTodoItemStatus(Integer itemId, TodoItemUpdateRequest request) {

        // 1. 현재 사용자 확인 (401 Unauthorized 방지)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. TodoItem 존재 확인 (404 Not Found)
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND)); // 리소스가 없으면 404

        // 3. 권한 확인 (본인 TodoList의 항목이 맞는지)
        // TodoItem -> TodoList -> User (소유자)
        if (!item.getTodoList().getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // 403 Forbidden 반환
        }

        // 4. 상태 업데이트
        item.setIsCompleted(request.getIsCompleted());

        // 5. 저장 (save는 @Transactional 때문에 생략 가능하나 명시적으로 호출)
        todoItemRepository.save(item);

        // 6. 응답 DTO 반환 (명세 반영)
        return TodoItemStatusResponse.builder()
                .todoItemId(item.getId())
                .isCompleted(item.getIsCompleted())
                .build();
    }
    /**
     * API 4-6: TODO 항목 삭제 로직 (Service)
     */
    @Transactional
    public TodoItemDeleteResponse deleteTodoItem(Integer todoItemId) {

        // 1. 현재 사용자 확인 (401 Unauthorized 방지)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. TodoItem 존재 확인 (404 Not Found)
        TodoItem item = todoItemRepository.findById(todoItemId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND)); // 리소스가 없으면 404

        // 3. 권한 확인 (403 Forbidden)
        // TodoItem -> TodoList -> User (소유자)를 확인
        if (!item.getTodoList().getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // 403 Forbidden 반환
        }

        // 4. 삭제
        todoItemRepository.delete(item);

        // 5. 응답 DTO 반환 (명세 반영)
        return TodoItemDeleteResponse.builder()
                .deletedTodoItemId(todoItemId)
                .build();
    }
    /**
     * API 4-7: 셀프 보고서 제출
     * * @param studyId Path Variable
     * @param request SelfReportCreateRequest DTO
     * @return SelfReportRewardResponse DTO
     */
    @Transactional
    public SelfReportRewardResponse submitSelfReport(Integer studyId, SelfReportCreateRequest request) {

        // 1. 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 2. 스터디 존재 확인 및 멤버 자격 확인 (404/403)
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 3. ⭐️ 중복 제출 확인 (409 Conflict)
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // ⭐️ SelfReportRepository에 existsBy... 메서드가 있어야 합니다.
        boolean alreadySubmitted = selfReportRepository.existsByStudyAndUserAndCreatedAtBetween(
                study, currentUser, startOfDay, endOfDay
        );

        if (alreadySubmitted) {
            throw new CustomException(ErrorStatus.REPORT_ALREADY_SUBMITTED); // 409 Conflict
        }

        // 4. 보고서 Entity 생성 및 저장
        SelfReport newReport = SelfReport.builder()
                .study(study)
                .user(currentUser)
                .subject(request.getSubject())
                .summary(request.getSummary())
                .content(request.getContent())
                .build();

        newReport = selfReportRepository.save(newReport);

        // 5. ⭐️ 보상 로직 적용 (HTML 태그 제거 후 글자 수 세기)
        int charCount = HtmlStripper.countStrippedCharacters(request.getContent()); // ⭐️ 유틸 사용

        SelfReportRewardResponse.RewardDto rewardDto = null;

        if (charCount >= 150) {
            // 5-1. 신뢰도 증가 로직
            UserProfile userProfile = userProfileRepository.findByUser(currentUser)
                    .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

            final int RELIABILITY_REWARD = 1;

            // UserProfile 업데이트 (엔티티에 updateReliabilityScore(int) 메서드가 있다고 가정)
            userProfile.updateReliabilityScore(userProfile.getReliabilityScore() + RELIABILITY_REWARD);
            userProfileRepository.save(userProfile); // 필요 시 명시적 저장

            // ReliabilityHistory 저장
            ReliabilityHistory history = ReliabilityHistory.builder()
                    .user(currentUser)
                    .changeAmount(RELIABILITY_REWARD)
                    .reason("셀프 보고서 150자 이상 작성")
                    .build();
            reliabilityHistoryRepository.save(history);

            // 응답 DTO에 보상 정보 채우기
            rewardDto = SelfReportRewardResponse.RewardDto.builder()
                    .type("RELIABILITY")
                    .changeAmount(RELIABILITY_REWARD)
                    .reason("셀프 보고서 150자 이상 작성")
                    .build();
        }

        // 6. 최종 응답 반환
        return SelfReportRewardResponse.builder()
                .selfReportId(newReport.getId())
                .reward(rewardDto)
                .build();
    }
    /**
     * API 4-8: 셀프 보고서 목록 조회 (페이지네이션)
     */
    public SelfReportListResponse getSelfReportList(Integer studyId, SelfReportListRequest request) {

        // 1. 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 스터디 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 확인 (멤버만 접근 가능 -> 403)
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 4. 페이지네이션 준비
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 5. DB 조회 (SelfReportRepository에 메서드 있어야 함!)
        Page<SelfReport> reportPage = selfReportRepository.findByStudyOrderByCreatedAtDesc(study, pageable);

        // 6. DTO 변환 (내용 100자 요약 처리)
        List<SelfReportListResponse.SelfReportSummaryDto> reportDtos = reportPage.getContent().stream()
                .map(report -> {
                    // HTML 태그 제거 후 순수 텍스트 추출
                    String plainText = HtmlStripper.stripTags(report.getContent());

                    // 100자 자르기 (길이가 100보다 작으면 그대로, 크면 자르고 "...")
                    String snippet = plainText.length() > 100
                            ? plainText.substring(0, 100) + "..."
                            : plainText;

                    return SelfReportListResponse.SelfReportSummaryDto.builder()
                            .reportId(report.getId())
                            .authorUsername(report.getUser().getUsername())
                            .contentSnippet(snippet)
                            .createdAt(report.getCreatedAt().toString())
                            .build();
                })
                .collect(Collectors.toList());

        // 7. 페이지 정보 조립
        SelfReportListResponse.PageInfoDto pageInfo = SelfReportListResponse.PageInfoDto.builder()
                .page(reportPage.getNumber())
                .size(reportPage.getSize())
                .totalPages(reportPage.getTotalPages())
                .totalElements(reportPage.getTotalElements())
                .build();

        return SelfReportListResponse.builder()
                .reports(reportDtos)
                .pageInfo(pageInfo)
                .build();
    }
}