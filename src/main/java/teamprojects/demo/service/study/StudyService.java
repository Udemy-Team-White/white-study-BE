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
import teamprojects.demo.repository.PointHistoryRepository;
import java.time.LocalTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyHasCategoryRepository studyHasCategoryRepository;
    private final UserRepository userRepository;
    private final StudyCategoryRepository studyCategoryRepository;
    private final UserProfileRepository userProfileRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final TodoListRepository todoListRepository;
    private final TodoItemRepository todoItemRepository;
    private final SelfReportRepository selfReportRepository;
    private final ReliabilityHistoryRepository reliabilityHistoryRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PraiseRepository praiseRepository;

    public List<CategoryResponse> getAllCategories() {
        return studyCategoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * API 1-5: 스터디 목록 조회
     */
    public StudyListResponse getStudyList(StudyListRequest request) {

        // 정렬 기준 설정
        Sort sort;
        if ("members".equals(request.getSortBy())) {
            sort = Sort.by(Sort.Direction.DESC, "maxMembers");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        // 페이지네이션 생성
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 검색 조건 생성 (Specification)
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
                    keywordPredicates.add(criteriaBuilder.or(titleLike, contentLike));
                }
                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // DB 조회
        Page<Study> studyPage = studyRepository.findAll(spec, pageable);

        // DTO 변환
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
                            .closedAt(study.getClosedAt() != null ? study.getClosedAt().toString() : null)
                            .status(study.getStatus().name())
                            .createdAt(study.getCreatedAt().toString())
                            .build();
                })
                .collect(Collectors.toList());

        // 페이지 정보 조립
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
     * API 2-1: 스터디 개설
     */
    @Transactional
    public StudyCreateResponse createStudy(StudyCreateRequest request) {

        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User leader = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        //studyType이 없으면 'ONLINE'으로 기본 설정
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

        //스터디 생성
        Study newStudy = Study.builder()
                .title(request.getTitle())
                .studyName(request.getStudyName())
                .content(request.getContent())
                .studyType(type)
                .maxMembers(request.getMaxMembers())
                .closedAt(request.getClosedAt())
                .endDate(request.getEndDate())
                .startDate(request.getStartDate())
                .status(Study.StudyStatus.RECRUITING)
                .leader(leader)
                .createdAt(LocalDateTime.now())
                .build();

        newStudy = studyRepository.save(newStudy);

        // 카테고리 연결 (Null 방어)
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

        // 스터디장 멤버 등록
        StudyMember leaderMember = StudyMember.builder()
                .study(newStudy)
                .user(leader)
                .role(StudyMember.StudyRole.LEADER)
                .build();

        studyMemberRepository.save(leaderMember);

        // 응답 반환
        return StudyCreateResponse.builder()
                .studyId(newStudy.getId())
                .build();
    }
    /**
     * API 2-2: 스터디 상세 정보 조회
     * (로그인 여부와 관계없이 조회 가능, 접속자 상태 userStatus 반환)
     */
    public StudyDetailResponse getStudyDetail(Integer studyId) {

        // 스터디 조회 (없으면 404)
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 카테고리 목록 조회
        List<StudyDetailResponse.CategoryDto> categoryDtos = studyHasCategoryRepository.findByStudy(study).stream()
                .map(shc -> StudyDetailResponse.CategoryDto.builder()
                        .categoryId(shc.getStudyCategory().getId())
                        .name(shc.getStudyCategory().getCategoryName())
                        .build())
                .collect(Collectors.toList());

        // 스터디장 정보 조회 (UserProfile에서 신뢰도 가져오기)
        User leader = study.getLeader();
        UserProfile leaderProfile = userProfileRepository.findByUser(leader)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        StudyDetailResponse.StudyLeaderDto leaderDto = StudyDetailResponse.StudyLeaderDto.builder()
                .username(leader.getUsername())
                .reliabilityScore(leaderProfile.getReliabilityScore())
                .build();

        // 현재 접속자의 상태(userStatus) 판별
        String userStatus = determineUserStatus(study);

        // 현재 멤버 수 조회
        Integer currentMembers = studyMemberRepository.countByStudy(study);

        // DTO 조립 및 반환
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
                        .endDate(study.getEndDate() != null ? study.getEndDate().toString() : null)
                        .build())
                .categories(categoryDtos)
                .studyLeader(leaderDto)
                .userStatus(userStatus)
                .build();
    }

    // 유저 상태 판별 로직
    private String determineUserStatus(Study study) {
        // 비로그인 사용자 (GUEST)
        Integer currentUserId = SecurityUtils.getCurrentUserId().orElse(null);
        if (currentUserId == null) {
            return "GUEST";
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 스터디장 (LEADER)
        if (study.getLeader().getId().equals(currentUserId)) {
            return "LEADER";
        }

        // 이미 참여 중인 멤버 (MEMBER)
        if (studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            return "MEMBER";
        }

        // 신청 후 승인 대기 중 (APPLIED) - Enum 타입으로 안전하게 비교!
        boolean isPending = studyApplicationRepository.findByUserAndStudy(currentUser, study)
                .stream()
                .anyMatch(app -> app.getStatus() == StudyApplication.ApplicationStatus.PENDING);

        if (isPending) {
            return "APPLIED";
        }

        //아무 관계 없는 로그인 사용자 (NONE)
        return "NONE";
    }

    /**
     * API 2-3: 스터디 참여 신청
     */
    @Transactional
    public StudyApplyResponse applyToStudy(Integer studyId, StudyApplyRequest request) {

        // 유저 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User applicant = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 스터디 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 모집 상태 확인
        // 모집 중 이거나 진행 중&추가모집 상태면 신청 가능!
        boolean isRecruiting = study.getStatus() == Study.StudyStatus.RECRUITING ||
                study.getStatus() == Study.StudyStatus.RECRUITING_IN_PROGRESS;

        if (!isRecruiting) {
            throw new CustomException(ErrorStatus.RECRUITMENT_CLOSED);
        }

        //정원 초과 확인
        Integer currentMemberCount = studyMemberRepository.countByStudy(study);
        if (currentMemberCount >= study.getMaxMembers()) {
            // 인원이 꽉 찼으면 에러 발생
            throw new CustomException(ErrorStatus.RECRUITMENT_CLOSED);
        }

        // 중복 확인

        // 이미 멤버인지 확인
        if (studyMemberRepository.existsByUserAndStudy(applicant, study)) {
            throw new CustomException(ErrorStatus.ALREADY_MEMBER_OR_APPLIED);
        }

        // 이미 신청했는지 확인 (Enum 안전 비교)
        // 리포지토리 메서드(exists...String) 대신, 리스트를 가져와서 스트림으로 검사합니다.
        boolean alreadyApplied = studyApplicationRepository.findByUserAndStudy(applicant, study)
                .stream()
                .anyMatch(app -> app.getStatus() == StudyApplication.ApplicationStatus.PENDING);

        if (alreadyApplied) {
            throw new CustomException(ErrorStatus.ALREADY_MEMBER_OR_APPLIED);
        }

        // 저장
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
     * API 4-2: TODO 플래너 조회
     */
    @Transactional
    public TodoPlannerResponse getStudyTodos(Integer studyId, LocalDate requestDate) {

        //유저 & 스터디 검증
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 날짜 계산
        LocalDate baseDate = requestDate;
        if ("WEEKLY".equalsIgnoreCase(study.getTodoCycle())) {
            baseDate = requestDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        }

        LocalDate targetDateForQuery = baseDate.plusWeeks(1);

        LocalDateTime startOfDay = targetDateForQuery.atStartOfDay();
        LocalDateTime endOfDay = targetDateForQuery.atTime(23, 59, 59);

        // DB 조회
        TodoList todoList = todoListRepository.findFirstByUserAndStudyAndTargetDateBetween(
                currentUser, study, startOfDay, endOfDay).orElse(null);

        // 없으면 자동 생성
        if (todoList == null) {
            String defaultTitle = targetDateForQuery.toString() + " 목표";

            todoList = TodoList.builder()
                    .study(study)
                    .user(currentUser)
                    .title(defaultTitle)
                    .targetDate(targetDateForQuery.atStartOfDay())
                    .build();

            todoList = todoListRepository.save(todoList);
        }

        // DTO 변환
        List<TodoItem> items = (todoList.getTodoItems() != null) ? todoList.getTodoItems() : new ArrayList<>();
        List<TodoPlannerResponse.TodoItemDto> itemDtos = items.stream()
                .map(todoItem -> TodoPlannerResponse.TodoItemDto.builder()
                        .todoItemId(todoItem.getId())
                        .content(todoItem.getContent())
                        .isCompleted(todoItem.getIsCompleted())
                        .build())
                .collect(Collectors.toList());

        return TodoPlannerResponse.builder()
                .todoListId(todoList.getId())
                .title(todoList.getTitle())
                .cycleStartDate(baseDate.toString())
                .targetDate(todoList.getTargetDate().toLocalDate().toString())
                .createdDate(todoList.getCreatedAt().toLocalDate().toString())
                .items(itemDtos)
                .build();
    }

    /**
     * API 4-3: TODO 플래너(그룹) 생성
     */
    @Transactional
    public TodoListCreateResponse createTodoList(Integer studyId, TodoListCreateRequest request) {
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus.STUDY_NOT_FOUND);
        }

        // 가장 최근 투두리스트 조회
        TodoList lastTodoList = todoListRepository.findTop1ByUserAndStudyOrderByTargetDateDesc(currentUser, study)
                .orElse(null);

        // 이미 미래거나 오늘 날짜인 플래너가 있으면 그거 반환 (생성 X)
        if (lastTodoList != null) {
            LocalDate lastDate = lastTodoList.getTargetDate().toLocalDate();
            LocalDate today = LocalDate.now();

            // 마지막 플래너가 오늘보다 뒤(미래)거나 오늘이라면 바로 리턴
            if (!lastDate.isBefore(today)) {
                return TodoListCreateResponse.builder()
                        .todoListId(lastTodoList.getId())
                        .title(lastTodoList.getTitle())
                        .targetDate(lastTodoList.getTargetDate())
                        .items(new ArrayList<>())
                        .build();
            }
        }

        // 날짜 계산
        LocalDateTime nextTargetDate;

        nextTargetDate = LocalDateTime.now();

        // 동시성 방어
        LocalDateTime startOfDay = nextTargetDate.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = nextTargetDate.toLocalDate().atTime(LocalTime.MAX);

        Optional<TodoList> existingTodayList = todoListRepository.findFirstByUserAndStudyAndTargetDateBetween(
                currentUser, study, startOfDay, endOfDay
        );

        if (existingTodayList.isPresent()) {
            TodoList exist = existingTodayList.get();
            return TodoListCreateResponse.builder()
                    .todoListId(exist.getId())
                    .title(exist.getTitle())
                    .targetDate(exist.getTargetDate())
                    .items(new ArrayList<>())
                    .build();
        }

        // 제목 및 저장
        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            title = nextTargetDate.toLocalDate().toString() + " 목표";
        }

        TodoList newTodoList = TodoList.builder()
                .study(study)
                .user(currentUser)
                .title(title)
                .targetDate(nextTargetDate)
                .build();

        newTodoList = todoListRepository.save(newTodoList);

        return TodoListCreateResponse.builder()
                .todoListId(newTodoList.getId())
                .title(newTodoList.getTitle())
                .targetDate(newTodoList.getTargetDate())
                .items(new ArrayList<>())
                .build();
    }

    /**
     * API 4-4: TODO 항목 생성 로직
     */
    @Transactional
    public TodoItemResponse createTodoItem(Integer listId, TodoItemCreateRequest request) {

        // 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // TodoList 존재 확인
        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 권한 확인
        if (!todoList.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 순서(orderIndex) 계산
        int nextOrder = (todoList.getTodoItems() == null) ? 1 : todoList.getTodoItems().size() + 1;

        // TodoItem Entity 생성
        TodoItem newItem = TodoItem.builder()
                .todoList(todoList)
                .content(request.getContent())
                .isCompleted(false)
                .orderIndex(nextOrder)
                .build();

        newItem = todoItemRepository.save(newItem);

        // 응답 DTO 반환
        return TodoItemResponse.builder()
                .todoItemId(newItem.getId())
                .content(newItem.getContent())
                .isCompleted(newItem.getIsCompleted())
                .build();
    }
    /**
     * API 4-5: TODO 항목 완료 상태 변경
     */
    @Transactional
    public TodoItemStatusResponse updateTodoItemStatus(Integer itemId, TodoItemUpdateRequest request) {

        // 유저 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 항목 조회
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 권한 체크
        User user = item.getTodoList().getUser();
        if (!user.getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        boolean newStatus = request.getIsCompleted();
        boolean oldStatus = item.getIsCompleted();

        // 상태가 변경되었을 때만 로직 실행
        if (newStatus != oldStatus) {
            UserProfile profile = userProfileRepository.findByUser(user)
                    .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

            int POINT_REWARD = 10;
            int RELIABILITY_REWARD = 2;

            if (newStatus) {
                // [완료 처리]
                profile.updatePoints(profile.getPoints() + POINT_REWARD);

                // 신뢰도: addReliabilityScore 메서드 사용 (현재 점수 조회 X, 더할 값만 넣기)
                profile.addReliabilityScore(RELIABILITY_REWARD);

                // 내역 기록
                pointHistoryRepository.save(PointHistory.builder()
                        .user(user)
                        .amount(POINT_REWARD)
                        .reason("TODO 완료 보상")
                        .build());

                reliabilityHistoryRepository.save(ReliabilityHistory.builder()
                        .user(user)
                        .changeAmount(RELIABILITY_REWARD)
                        .reason("TODO 완료 보상")
                        .build());

            } else {
                // [완료 취소]
                profile.updatePoints(profile.getPoints() - POINT_REWARD);

                // 신뢰도: 음수 값을 넣어서 차감
                profile.addReliabilityScore(-RELIABILITY_REWARD);

                // 내역 기록
                pointHistoryRepository.save(PointHistory.builder()
                        .user(user)
                        .amount(-POINT_REWARD)
                        .reason("TODO 완료 취소")
                        .build());

                reliabilityHistoryRepository.save(ReliabilityHistory.builder()
                        .user(user)
                        .changeAmount(-RELIABILITY_REWARD)
                        .reason("TODO 완료 취소")
                        .build());
            }
            // Dirty Checking으로 저장되지만 명시적으로 save 호출해도 무방
            userProfileRepository.save(profile);
        }

        // 상태 업데이트 및 저장
        item.setIsCompleted(newStatus);
        todoItemRepository.save(item);

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

        // 현재 사용자 확인 (401 Unauthorized 방지)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // TodoItem 존재 확인 (404 Not Found)
        TodoItem item = todoItemRepository.findById(todoItemId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND)); // 리소스가 없으면 404

        // 권한 확인 (403 Forbidden)
        // TodoItem -> TodoList -> User (소유자)를 확인
        if (!item.getTodoList().getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // 403 Forbidden 반환
        }

        // 삭제
        todoItemRepository.delete(item);

        //응답 DTO 반환 (명세 반영)
        return TodoItemDeleteResponse.builder()
                .deletedTodoItemId(todoItemId)
                .build();
    }
    /**
     * API 4-7: 셀프 보고서 제출
     */
    @Transactional
    public SelfReportRewardResponse submitSelfReport(Integer studyId, SelfReportCreateRequest request) {

        //현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        //스터디 존재 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 스터디 진행 상태 체크
        if (study.getStatus() != Study.StudyStatus.IN_PROGRESS &&
                study.getStatus() != Study.StudyStatus.RECRUITING_IN_PROGRESS) {
            throw new CustomException(ErrorStatus.STUDY_TERMINATED);
        }

        // 멤버 자격 확인
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 중복 제출 확인 (오늘 날짜 기준)
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        boolean alreadySubmitted = selfReportRepository.existsByStudyAndUserAndCreatedAtBetween(
                study, currentUser, startOfDay, endOfDay
        );

        if (alreadySubmitted) {
            throw new CustomException(ErrorStatus.REPORT_ALREADY_SUBMITTED);
        }

        // 보고서 저장
        SelfReport newReport = SelfReport.builder()
                .study(study)
                .user(currentUser)
                .subject(request.getSubject())
                .summary(request.getSummary())
                .content(request.getContent())
                .build();

        newReport = selfReportRepository.save(newReport);

        // 글자 수 확인 및 보상 처리
        int charCount = HtmlStripper.countStrippedCharacters(request.getContent());

        SelfReportRewardResponse.RewardDto rewardDto = null;

        if (charCount >= 150) {
            UserProfile userProfile = userProfileRepository.findByUser(currentUser)
                    .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

            final int RELIABILITY_REWARD = 1;
            userProfile.addReliabilityScore(RELIABILITY_REWARD);
            userProfileRepository.save(userProfile);

            ReliabilityHistory history = ReliabilityHistory.builder()
                    .user(currentUser)
                    .changeAmount(RELIABILITY_REWARD)
                    .reason("셀프 보고서 150자 이상 작성")
                    .build();
            reliabilityHistoryRepository.save(history);

            rewardDto = SelfReportRewardResponse.RewardDto.builder()
                    .type("RELIABILITY")
                    .changeAmount(RELIABILITY_REWARD)
                    .reason("셀프 보고서 150자 이상 작성")
                    .build();
        }

        return SelfReportRewardResponse.builder()
                .selfReportId(newReport.getId())
                .reward(rewardDto)
                .build();
    }
    /**
     * API 4-8: 셀프 보고서 목록 조회
     */
    public SelfReportListResponse getSelfReportList(Integer studyId, SelfReportListRequest request) {

        //유저 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 스터디 확인
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 권한 확인
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 페이지네이션
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<SelfReport> reportPage = selfReportRepository.findByStudyOrderByCreatedAtDesc(study, pageable);

        // DTO 변환
        List<SelfReportListResponse.SelfReportSummaryDto> reportDtos = reportPage.getContent().stream()
                .map(report -> {

                    return SelfReportListResponse.SelfReportSummaryDto.builder()
                            .reportId(report.getId())
                            .authorUsername(report.getUser().getUsername())
                            .contentSnippet(report.getSubject())

                            .createdAt(report.getCreatedAt().toString())
                            .build();
                })
                .collect(Collectors.toList());

        // 페이지 정보
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
    /**
     * API 4-9: 셀프 보고서 상세 조회
     * URL: /api/reports/{reportId}
     */
    public SelfReportDetailResponse getSelfReportDetail(Integer reportId) {

        // 현재 사용자 확인 (비로그인이면 401)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 보고서 존재 확인 (404)
        SelfReport report = selfReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 권한 체크 (403 Forbidden)
        // 보고서가 속한 스터디의 멤버여야만 볼 수 있습니다.
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, report.getStudy())) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        //내가 쓴 글인지 확인 (isMine)
        boolean isMine = report.getUser().getId().equals(currentUserId);

        // DTO 반환
        return SelfReportDetailResponse.builder()
                .reportId(report.getId())
                .authorUsername(report.getUser().getUsername())
                .subject(report.getSubject())
                .summary(report.getSummary())
                .content(report.getContent())
                .createdAt(report.getCreatedAt().toString())
                .isMine(isMine)
                .build();
    }
    /**
     * API 4-10: 셀프 보고서 수정
     */
    @Transactional
    public SelfReportUpdateResponse updateSelfReport(Integer reportId, SelfReportUpdateRequest request) {

        //현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        //보고서 조회 (없으면 404)
        SelfReport report = selfReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        //권한 체크 (작성자 본인 확인 -> 403)
        if (!report.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        //내용 수정
        report.updateContent(request.getContent());

        // 만약 제목/요약도 수정한다면
        if (request.getSubject() != null) report.updateSubject(request.getSubject());
        if (request.getSummary() != null) report.updateSummary(request.getSummary());

        //명시적 저장 (updateAt 갱신을 위해)
        selfReportRepository.save(report);

        //응답 DTO 반환
        return SelfReportUpdateResponse.builder()
                .reportId(report.getId())
                .content(report.getContent())
                .updatedAt(report.getUpdatedAt().toString())
                .build();
    }
    /**
     * API 4-11: 셀프 보고서 삭제
     */
    @Transactional
    public SelfReportDeleteResponse deleteSelfReport(Integer reportId) {

        //현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 보고서 조회 (없으면 404)
        SelfReport report = selfReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        //권한 체크 (작성자 본인 확인 -> 403)
        if (!report.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 삭제
        selfReportRepository.delete(report);

        // 응답 DTO 반환
        return SelfReportDeleteResponse.builder()
                .deletedReportId(reportId)
                .build();
    }

    /**
     * API 5-1: 스터디 신청자 목록 조회 (스터디장 전용)
     */
    public List<StudyApplicantResponse> getApplicants(Integer studyId) {

        //현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        //스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        //권한 체크 (스터디장이 아니면 403)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        //신청자 목록 조회 (PENDING 상태만)
        List<StudyApplication> applications = studyApplicationRepository.findByStudyIdAndStatus(
                studyId,
                StudyApplication.ApplicationStatus.PENDING
        );

        //DTO 변환
        return applications.stream()
                .map(app -> {
                    User applicant = app.getUser();
                    UserProfile profile = userProfileRepository.findByUser(applicant)
                            .orElse(UserProfile.builder().user(applicant).build()); // 없을 경우 빈 객체 처리
                    List<String> equippedItems = new ArrayList<>();

                    StudyApplicantResponse.ApplicantDto userDto = StudyApplicantResponse.ApplicantDto.builder()
                            .userId(applicant.getId())
                            .username(applicant.getUsername())
                            .profileImageUrl(profile.getProfileImageUrl())
                            .reliabilityScore(profile.getReliabilityScore())
                            .equippedItems(equippedItems) // 아이템 리스트
                            .build();

                    return StudyApplicantResponse.builder()
                            .applicationId(app.getId())
                            .message(app.getMessage())
                            .appliedAt(app.getCreatedAt().toString())
                            .user(userDto) // 중첩된 유저 정보
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * API 5-2: 스터디 신청 승인
     */
    @Transactional
    public StudyApplicantApproveResponse approveApplicant(Integer studyId, Integer applicationId, StudyApplicantApproveRequest request) {

        //현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        //스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        //권한 체크 (리더만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // 스터디 관리 권한 없음
        }

        //신청서 조회 (해당 스터디의 신청서인지 확인)
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        if (!application.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 잘못된 요청 (다른 스터디 신청서)
        }

        // 이미 처리된 신청서인지 확인
        if (application.getStatus() != StudyApplication.ApplicationStatus.PENDING) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 이미 승인/거절된 신청서
        }

        // 모집 인원 체크 (409 Conflict)
        Integer currentMembers = studyMemberRepository.countByStudy(study);
        if (currentMembers >= study.getMaxMembers()) {
            throw new CustomException(ErrorStatus.RECRUITMENT_CLOSED); // 혹은 별도의 FULL 에러 코드
        }

        // 신청 상태 변경 (PENDING -> APPROVED)
        application.updateStatus(StudyApplication.ApplicationStatus.APPROVED);

        //정식 멤버로 등록 (INSERT)
        StudyMember newMember = StudyMember.builder()
                .study(study)
                .user(application.getUser())
                .role(StudyMember.StudyRole.MEMBER) // 기본값 MEMBER
                .build();

        newMember = studyMemberRepository.save(newMember);

        //응답 DTO 반환
        return StudyApplicantApproveResponse.builder()
                .memberId(newMember.getId())
                .userId(newMember.getUser().getId())
                .username(newMember.getUser().getUsername())
                .role(newMember.getRole().name())
                .build();
    }
    /**
     * API 5-3: 스터디 신청 거절
     */
    @Transactional
    public StudyApplicantRejectResponse rejectApplicant(Integer studyId, Integer applicationId, StudyApplicantRejectRequest request) {

        //현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        //스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        //권한 체크 (리더만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        //신청서 조회
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        if (!application.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 다른 스터디 신청서임
        }

        // 이미 처리된 신청서인지 확인
        if (application.getStatus() != StudyApplication.ApplicationStatus.PENDING) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        //신청 상태 변경 (PENDING -> REJECTED)
        application.updateStatus(StudyApplication.ApplicationStatus.REJECTED);

        //응답 DTO 반환
        return StudyApplicantRejectResponse.builder()
                .applicationId(application.getId())
                .status(application.getStatus().name())
                .build();
    }

    /**
     * API 5-4: 확정 멤버 목록 조회
     */
    public StudyMemberResponse getStudyMembers(Integer studyId) {

        //현재 접속한 사용자 ID 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 생략 가능하지만 유지해도 무방함
        // User currentUser = userRepository.findById(currentUserId) ...;

        // 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 나의 상태 판별
        String myStatus = determineUserStatus(study);

        // 멤버 목록 조회
        List<StudyMember> members = studyMemberRepository.findByStudyIdWithUser(studyId);

        // DTO 변환
        List<StudyMemberResponse.MemberDto> memberDtos = members.stream()
                .map(member -> {
                    User user = member.getUser();

                    // 프로필 조회 로직
                    UserProfile profile = userProfileRepository.findByUser(user)
                            .orElse(UserProfile.builder().user(user).build());
                    List<String> equippedItems = new ArrayList<>(); // 아이템 로직은 구현 필요 시 추가

                    StudyMemberResponse.UserDto userDto = StudyMemberResponse.UserDto.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .bio(profile.getIntroduction())
                            .imgUrl(profile.getProfileImageUrl())
                            .equippedItems(equippedItems)
                            .build();

                    // 현재 로그인한 유저와 멤버의 유저가 같은지 확인
                    boolean isMe = user.getId().equals(currentUserId);

                    return StudyMemberResponse.MemberDto.builder()
                            .memberId(member.getId())
                            .role(member.getRole().name())
                            .isMe(isMe) // true/false를 내려줍니다.
                            .user(userDto)
                            .build();
                })
                .collect(Collectors.toList());

        return StudyMemberResponse.builder()
                .myStatus(myStatus)
                .members(memberDtos)
                .build();
    }

    /**
     * API 5-5: 멤버 역할 변경
     */
    @Transactional
    public StudyMemberRoleUpdateResponse updateMemberRole(Integer studyId, Integer memberId, StudyMemberRoleUpdateRequest request) {

        //현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 권한 체크 (스터디장만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // 스터디 관리 권한 없음
        }

        // 대상 멤버 조회
        StudyMember member = studyMemberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND)); // 멤버 없음

        // 멤버가 해당 스터디 소속인지 확인
        if (!member.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        //역할 변경 (String -> Enum)
        try {
            StudyMember.StudyRole newRole = StudyMember.StudyRole.valueOf(request.getRole());

            // 본인(리더)의 역할은 변경 불가 (안전장치)
            if (member.getUser().getId().equals(currentUserId)) {
                throw new CustomException(ErrorStatus._BAD_REQUEST); // 리더 위임 기능이 아니라면 본인 변경 막음
            }

            member.updateRole(newRole);

        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 잘못된 역할 이름
        }

        // 저장
        studyMemberRepository.save(member);

        //응답 DTO 반환
        return StudyMemberRoleUpdateResponse.builder()
                .memberId(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .newRole(member.getRole().name())
                .build();
    }

    /**
     * API 5-6: 스터디 멤버 추방
     */
    @Transactional
    public StudyMemberKickResponse kickMember(Integer studyId, Integer memberId) {

        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        StudyMember member = studyMemberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        if (!member.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        if (member.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus.CANNOT_KICK_SELF);
        }

        studyMemberRepository.delete(member);

        return StudyMemberKickResponse.builder()
                .kickedMemberId(memberId)
                .message("정상적으로 처리되었습니다.")
                .build();
    }

    /**
     * API 5-7: 스터디 정보 수정
     */
    @Transactional
    public void updateStudy(Integer studyId, StudyUpdateRequest request) {

        // 유저 & 스터디 조회 & 권한 체크
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 정보 수정 (값이 있는 것만 수정)

        // 제목 & 이름
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            study.updateTitle(request.getTitle());
        }
        if (request.getStudyName() != null && !request.getStudyName().isBlank()) {
            study.updateStudyName(request.getStudyName());
        }

        // 내용 & 인원
        if (request.getContent() != null && !request.getContent().isBlank()) {
            study.updateContent(request.getContent());
        }
        if (request.getMaxMembers() != null) {
            study.updateMaxMembers(request.getMaxMembers());
        }

        // 기간 (시작일, 종료일)
        if (request.getStartDate() != null) {
            study.updateStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            study.updateEndDate(request.getEndDate());
        }

        // 스터디 타입 (ONLINE/OFFLINE) 수정
        if (request.getStudyType() != null && !request.getStudyType().isBlank()) {
            try {
                Study.StudyType newType = Study.StudyType.valueOf(request.getStudyType());
                study.updateStudyType(newType);
            } catch (IllegalArgumentException e) {
                // 잘못된 값이면 무시
            }
        }

        //카테고리 수정
        if (request.getCategoryIds() != null) {
            studyHasCategoryRepository.deleteByStudy(study);
            for (Integer categoryId : request.getCategoryIds()) {
                StudyCategory category = studyCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new CustomException(ErrorStatus.CATEGORY_NOT_FOUND));
                StudyHasCategory link = StudyHasCategory.builder()
                        .study(study)
                        .studyCategory(category)
                        .build();
                studyHasCategoryRepository.save(link);
            }
        }
    }
    /**
     * API 5-8: 스터디 일정 및 주기 수정
     */
    @Transactional
    public void updateStudySchedule(Integer studyId, StudyScheduleUpdateRequest request) {

        //리더 권한 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 시작일 변경 시 -> TodoList 날짜들도 같이 이동
        if (request.getStartDate() != null) {
            LocalDateTime oldStart = study.getStartDate();
            LocalDateTime newStart = request.getStartDate();

            // 날짜가 변경되었다면?
            if (oldStart != null && !oldStart.isEqual(newStart)) {
                // 두 날짜의 차이(일수) 계산
                long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(oldStart, newStart);

                // 이 스터디의 모든 TodoList 조회
                List<TodoList> todoLists = todoListRepository.findByStudy(study);

                // 모든 TodoList의 targetDate를 차이만큼 이동
                for (TodoList list : todoLists) {
                    // 기존 날짜 + 차이값
                    LocalDateTime newTargetDate = list.getTargetDate().plusDays(daysDiff);
                    list.updateTargetDate(newTargetDate);
                }
            }
            // 스터디 시작일 업데이트
            study.updateStartDate(newStart);
        }

        // 종료일 업데이트
        if (request.getEndDate() != null) {
            study.updateEndDate(request.getEndDate());
        }

        // 주기 업데이트
        if (request.getTodoCycle() != null && !request.getTodoCycle().isBlank()) {
            study.updateTodoCycle(request.getTodoCycle());
        }
    }

    /**
     * API 6-5: 칭찬 메시지 전송
     */
    @Transactional
    public PraiseResponse sendPraise(Integer studyId, PraiseCreateRequest request) {

        // 유저 및 스터디 조회
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 검증 로직
        if (sender.getId().equals(receiver.getId())) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        boolean isSenderMember = studyMemberRepository.existsByUserAndStudy(sender, study);
        boolean isReceiverMember = studyMemberRepository.existsByUserAndStudy(receiver, study);
        if (!isSenderMember || !isReceiverMember) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 칭찬 저장
        Praise praise = Praise.builder()
                .study(study)
                .sender(sender)
                .receiver(receiver)
                .message(request.getMessage())
                .isAnonymous(true)
                .build();
        praise = praiseRepository.save(praise);

        // 발신자 보상 지급 (포인트 0점 처리 유지)
        UserProfile senderProfile = userProfileRepository.findByUser(sender)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));
        int REWARD_POINTS = 0;

        // 0점이라도 로직상 업데이트 호출 (필요 없다면 이 줄 제거 가능)
        senderProfile.updatePoints(senderProfile.getPoints() + REWARD_POINTS);

        PointHistory senderHistory = PointHistory.builder()
                .user(sender)
                .amount(REWARD_POINTS)
                .reason("스터디원 칭찬하기")
                .build();
        pointHistoryRepository.save(senderHistory);

        // 수신자 신뢰도 상승 (신뢰도 +5)
        UserProfile receiverProfile = userProfileRepository.findByUser(receiver)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        int RELIABILITY_REWARD = 5; // 칭찬 받으면 5점 상승
        receiverProfile.addReliabilityScore(RELIABILITY_REWARD);

        // 수신자 히스토리 기록
        ReliabilityHistory receiverHistory = ReliabilityHistory.builder()
                .user(receiver)
                .changeAmount(RELIABILITY_REWARD)
                .reason("칭찬 받음: " + request.getMessage())
                .build();
        reliabilityHistoryRepository.save(receiverHistory);

        // 응답 생성
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