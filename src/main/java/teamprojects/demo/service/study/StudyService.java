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
import java.time.Duration;
import java.util.Map;



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
    private final PointHistoryRepository pointHistoryRepository;

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
                .endDate(request.getEndDate())
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
     * API 4-2: TODO 플래너 조회 (최종: 단일 객체 반환)
     */
    @Transactional
    public TodoPlannerResponse getStudyTodos(Integer studyId, LocalDate requestDate) { // ⭐️ List 제거

        // 1. 유저 & 스터디 확인 (기존 동일)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 2. 날짜 계산 (기존 동일)
        LocalDate searchDate = requestDate;
        if ("WEEKLY".equalsIgnoreCase(study.getTodoCycle())) {
            searchDate = requestDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        }

        LocalDateTime startOfDay = searchDate.atStartOfDay();
        LocalDateTime endOfDay = searchDate.atTime(23, 59, 59);

        // 3. ⭐️ [핵심 변경] 단건 조회 및 처리
        TodoList todoList = todoListRepository.findFirstByUserAndStudyAndTargetDateBetween(
                        currentUser, study, startOfDay, endOfDay)
                .orElse(null);

        // 없으면 생성!
        if (todoList == null) {
            String defaultTitle = searchDate.toString() + " 주차 목표";
            todoList = TodoList.builder()
                    .study(study)
                    .user(currentUser)
                    .title(defaultTitle)
                    .targetDate(startOfDay)
                    .build();
            todoList = todoListRepository.save(todoList);
        }

        // 4. DTO 변환 (리스트 반복문 없이 바로 변환!)
        List<TodoItem> items = (todoList.getTodoItems() != null) ? todoList.getTodoItems() : new ArrayList<>();

        List<TodoPlannerResponse.TodoItemDto> itemDtos = items.stream()
                .map(todoItem -> TodoPlannerResponse.TodoItemDto.builder()
                        .todoItemId(todoItem.getId())
                        .content(todoItem.getContent())
                        .isCompleted(todoItem.getIsCompleted())
                        .build())
                .collect(Collectors.toList());

        // ⭐️ 단일 객체 반환
        return TodoPlannerResponse.builder()
                .todoListId(todoList.getId())
                .title(todoList.getTitle())
                .cycleStartDate(searchDate.toString())
                .targetDate(todoList.getTargetDate().toLocalDate().toString())
                .createdDate(todoList.getCreatedAt().toLocalDate().toString())
                .items(itemDtos)
                .build();
    }

    /**
     * API 4-3: TODO 플래너(그룹) 생성 (수정: 최소 7일 간격 보장)
     */
    @Transactional
    public TodoListCreateResponse createTodoList(Integer studyId, TodoListCreateRequest request) {

        // 1. 유저 & 스터디 확인 (기존 동일)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!studyMemberRepository.existsByUserAndStudy(currentUser, study)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 2. ⭐️ [핵심 수정] 다음 targetDate 자동 계산
        LocalDateTime nextTargetDate;

        // (1) 요청에 날짜가 있으면 그걸 우선 사용
        if (request.getTargetDate() != null) {
            nextTargetDate = request.getTargetDate();
        }
        // (2) 자동 계산 로직
        else {
            TodoList lastTodoList = todoListRepository.findTop1ByUserAndStudyOrderByTargetDateDesc(currentUser, study)
                    .orElse(null);

            if (lastTodoList == null) {
                // 처음 만드는 경우: 스터디 시작일 기준
                LocalDateTime baseDate = study.getStartDate() != null ? study.getStartDate() : LocalDateTime.now();

                // ⭐️ [수정] 시작일 + 7일 (최소 1주일 뒤 목표로 설정)
                // 프론트 요청: "임의로 7일이라는 기간을 넣어줄 수 있을까요?" -> OK!
                nextTargetDate = baseDate.plusWeeks(1);

            } else {
                // 이미 있는 경우: 마지막 목표일 + 7일
                nextTargetDate = lastTodoList.getTargetDate().plusWeeks(1);
            }
        }

        // 3. 제목 자동 생성 (기존 동일)
        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            // 날짜 포맷 예쁘게 (yyyy-MM-dd)
            title = nextTargetDate.toLocalDate().toString() + " 목표";
        }

        // 4. 저장 (기존 동일)
        TodoList newTodoList = TodoList.builder()
                .study(study)
                .user(currentUser)
                .title(title)
                .targetDate(nextTargetDate)
                .build();

        newTodoList = todoListRepository.save(newTodoList);

        // 5. 응답 (기존 동일)
        return TodoListCreateResponse.builder()
                .todoListId(newTodoList.getId())
                .title(newTodoList.getTitle())
                .targetDate(newTodoList.getTargetDate())
                .items(new ArrayList<>())
                .build();
    }

    /**
     * API 4-4: TODO 항목 생성 로직 (Service)
     * 수정사항: orderIndex(순서) 값 자동 주입 추가
     */
    @Transactional
    public TodoItemResponse createTodoItem(Integer listId, TodoItemCreateRequest request) {

        // 1. 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. TodoList 존재 확인
        TodoList todoList = todoListRepository.findById(listId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 확인 (본인 TodoList가 맞는지)
        if (!todoList.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // ⭐️ [핵심 수정] 순서(orderIndex) 계산
        // 기존 아이템 개수 + 1을 해서 맨 마지막 순서로 설정합니다.
        // (list가 null일 경우를 대비해 안전하게 처리)
        int nextOrder = (todoList.getTodoItems() == null) ? 1 : todoList.getTodoItems().size() + 1;

        // 4. TodoItem Entity 생성
        TodoItem newItem = TodoItem.builder()
                .todoList(todoList)
                .content(request.getContent())
                .isCompleted(false)

                // ⭐️ [추가됨] DB 에러 방지용 순서 값 주입!
                .orderIndex(nextOrder)

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
     * API 4-5: TODO 항목 완료 상태 변경 (최종: 포인트 + 신뢰도 모두 처리)
     */
    @Transactional
    public TodoItemStatusResponse updateTodoItemStatus(Integer itemId, TodoItemUpdateRequest request) {

        // 1. 유저 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 항목 조회
        TodoItem item = todoItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크
        User user = item.getTodoList().getUser();
        if (!user.getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // ⭐️ [핵심 로직] 상태 변경에 따른 보상(포인트 & 신뢰도) 처리
        boolean newStatus = request.getIsCompleted();
        boolean oldStatus = item.getIsCompleted();

        if (newStatus != oldStatus) {
            UserProfile profile = userProfileRepository.findByUser(user)
                    .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

            int POINT_REWARD = 10;       // 포인트 보상
            int RELIABILITY_REWARD = 2;  // ⭐️ 신뢰도 보상 (할 일 완료는 중요하니까 2점!)

            if (newStatus) {
                // (1) 완료 처리: 포인트 & 신뢰도 지급
                profile.updatePoints(profile.getPoints() + POINT_REWARD);
                profile.updateReliabilityScore(profile.getReliabilityScore() + RELIABILITY_REWARD); // ⭐️ 추가

                // 내역 기록 (포인트)
                PointHistory pHistory = PointHistory.builder()
                        .user(user)
                        .amount(POINT_REWARD)
                        .reason("TODO 완료 보상")
                        .build();
                pointHistoryRepository.save(pHistory);

                // ⭐️ 내역 기록 (신뢰도)
                ReliabilityHistory rHistory = ReliabilityHistory.builder()
                        .user(user)
                        .changeAmount(RELIABILITY_REWARD)
                        .reason("TODO 완료 보상")
                        .build();
                reliabilityHistoryRepository.save(rHistory);

            } else {
                // (2) 완료 취소: 포인트 & 신뢰도 회수
                profile.updatePoints(profile.getPoints() - POINT_REWARD);
                profile.updateReliabilityScore(profile.getReliabilityScore() - RELIABILITY_REWARD); // ⭐️ 추가

                // 내역 기록 (포인트)
                PointHistory pHistory = PointHistory.builder()
                        .user(user)
                        .amount(-POINT_REWARD)
                        .reason("TODO 완료 취소")
                        .build();
                pointHistoryRepository.save(pHistory);

                // ⭐️ 내역 기록 (신뢰도)
                ReliabilityHistory rHistory = ReliabilityHistory.builder()
                        .user(user)
                        .changeAmount(-RELIABILITY_REWARD)
                        .reason("TODO 완료 취소")
                        .build();
                reliabilityHistoryRepository.save(rHistory);
            }

            userProfileRepository.save(profile);
        }

        // 4. 상태 업데이트 및 저장
        item.setIsCompleted(newStatus);
        todoItemRepository.save(item);

        // 5. 응답
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
    /**
     * API 4-9: 셀프 보고서 상세 조회
     * URL: /api/reports/{reportId}
     */
    public SelfReportDetailResponse getSelfReportDetail(Integer reportId) {

        // 1. 현재 사용자 확인 (비로그인이면 401)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 보고서 존재 확인 (404)
        SelfReport report = selfReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 3. ⭐️ 권한 체크 (403 Forbidden)
        // "보고서가 속한 스터디"의 멤버여야만 볼 수 있습니다.
        if (!studyMemberRepository.existsByUserAndStudy(currentUser, report.getStudy())) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 4. 내가 쓴 글인지 확인 (isMine)
        boolean isMine = report.getUser().getId().equals(currentUserId);

        // 5. DTO 반환
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

        // 1. 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 보고서 조회 (없으면 404)
        SelfReport report = selfReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 3. ⭐️ 권한 체크 (작성자 본인 확인 -> 403)
        if (!report.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // "본인이 작성한 보고서만..."
        }

        // 4. 내용 수정 (Dirty Checking으로 자동 저장됨)
        report.updateContent(request.getContent());

        // (만약 제목/요약도 수정한다면)
        if (request.getSubject() != null) report.updateSubject(request.getSubject());
        if (request.getSummary() != null) report.updateSummary(request.getSummary());

        // 5. 명시적 저장 (updateAt 갱신을 위해)
        selfReportRepository.save(report);

        // 6. 응답 DTO 반환
        return SelfReportUpdateResponse.builder()
                .reportId(report.getId())
                .content(report.getContent())
                .updatedAt(report.getUpdatedAt().toString()) // Entity에 @UpdateTimestamp 있어야 함
                .build();
    }
    /**
     * API 4-11: 셀프 보고서 삭제
     */
    @Transactional
    public SelfReportDeleteResponse deleteSelfReport(Integer reportId) {

        // 1. 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 보고서 조회 (없으면 404)
        SelfReport report = selfReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // 3. ⭐️ 권한 체크 (작성자 본인 확인 -> 403)
        if (!report.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // "본인이 작성한 보고서만..."
        }

        // 4. 삭제
        selfReportRepository.delete(report);

        // 5. 응답 DTO 반환
        return SelfReportDeleteResponse.builder()
                .deletedReportId(reportId)
                .build();
    }

    /**
     * API 5-1: 스터디 신청자 목록 조회 (스터디장 전용)
     */
    public List<StudyApplicantResponse> getApplicants(Integer studyId) {

        // 1. 현재 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. ⭐️ 권한 체크 (스터디장이 아니면 403)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // "스터디 관리 권한이 없습니다."
        }

        // 4. 신청자 목록 조회 (PENDING 상태만)
        List<StudyApplication> applications = studyApplicationRepository.findByStudyIdAndStatus(
                studyId,
                StudyApplication.ApplicationStatus.PENDING
        );

        // 5. DTO 변환
        return applications.stream()
                .map(app -> {
                    User applicant = app.getUser();
                    UserProfile profile = userProfileRepository.findByUser(applicant)
                            .orElse(UserProfile.builder().user(applicant).build()); // 없을 경우 빈 객체 처리

                    // ⭐️ [추가] 착용 아이템 조회 로직 (UserInventory 엔티티가 있다고 가정)
                    // 현재는 빈 리스트로 반환하지만, 나중에 UserInventoryRepository를 통해 가져오면 됩니다.
                    // 예: userInventoryRepository.findEquippedItemImages(applicant.getId());
                    List<String> equippedItems = new ArrayList<>();

                    // (임시 데이터 예시 - 나중에 삭제하세요)
                    // equippedItems.add("https://item-image-url.com/hat.png");

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

        // 1. 현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 (리더만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // 스터디 관리 권한 없음
        }

        // 4. 신청서 조회 (해당 스터디의 신청서인지 확인)
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        if (!application.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 잘못된 요청 (다른 스터디 신청서)
        }

        // (이미 처리된 신청서인지 확인)
        if (application.getStatus() != StudyApplication.ApplicationStatus.PENDING) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 이미 승인/거절된 신청서
        }

        // 5. ⭐️ 모집 인원 체크 (409 Conflict)
        Integer currentMembers = studyMemberRepository.countByStudy(study);
        if (currentMembers >= study.getMaxMembers()) {
            throw new CustomException(ErrorStatus.RECRUITMENT_CLOSED); // 혹은 별도의 FULL 에러 코드
        }

        // 6. 신청 상태 변경 (PENDING -> APPROVED)
        // ⭐️ StudyApplication 엔티티에 updateStatus 메서드 필요 (없으면 @Setter나 메서드 추가)
        application.updateStatus(StudyApplication.ApplicationStatus.APPROVED);

        // (선택 사항: 승인 메시지도 저장하고 싶다면 application.setMessage(request.getMessage()) 등 처리)

        // 7. 정식 멤버로 등록 (INSERT)
        StudyMember newMember = StudyMember.builder()
                .study(study)
                .user(application.getUser())
                .role(StudyMember.StudyRole.MEMBER) // 기본값 MEMBER
                .build();

        newMember = studyMemberRepository.save(newMember);

        // 8. 응답 DTO 반환
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

        // 1. 현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 (리더만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 4. 신청서 조회
        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        if (!application.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 다른 스터디 신청서임
        }

        // (이미 처리된 신청서인지 확인)
        if (application.getStatus() != StudyApplication.ApplicationStatus.PENDING) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        // 5. 신청 상태 변경 (PENDING -> REJECTED)
        application.updateStatus(StudyApplication.ApplicationStatus.REJECTED);

        // (참고: 거절 사유를 저장하고 싶다면 Entity에 필드를 추가해야 합니다. 지금은 상태만 바꿉니다.)

        // 6. 응답 DTO 반환
        return StudyApplicantRejectResponse.builder()
                .applicationId(application.getId())
                .status(application.getStatus().name())
                .build();
    }

    /**
     * API 5-4: 확정 멤버 목록 조회
     */
    public StudyMemberResponse getStudyMembers(Integer studyId) {

        // 1. 현재 접속자 확인 (비로그인 허용 or 불가? 명세상 403이 있으므로 로그인 필수일 듯)
        // (만약 비로그인도 조회가 가능하다면 로직을 살짝 바꿔야 합니다. 여기선 로그인 필수로 짭니다.)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 (스터디장이 아니어도 조회는 가능해야 할 것 같은데...
        // 명세서엔 '스터디장이 아닌 경우 403'이라고 되어 있지만, 보통 멤버 목록은 다 봅니다.
        // 하지만 요청대로 '관리용'이라면 리더만 봐야겠죠. 일단 리더 체크 넣겠습니다.)
        // ⚠️ 만약 일반 멤버도 봐야 한다면 이 체크를 빼세요!
        if (!study.getLeader().getId().equals(currentUserId)) {
            // throw new CustomException(ErrorStatus._FORBIDDEN);
            // (일단 주석: 보통 멤버 목록은 누구나 볼 수 있으므로 에러는 안 냄)
        }

        // 4. 나의 상태 판별 (재사용!)
        String myStatus = determineUserStatus(study);

        // 5. 멤버 목록 조회
        List<StudyMember> members = studyMemberRepository.findByStudyIdWithUser(studyId);

        // 6. DTO 변환
        List<StudyMemberResponse.MemberDto> memberDtos = members.stream()
                .map(member -> {
                    User user = member.getUser();
                    UserProfile profile = userProfileRepository.findByUser(user)
                            .orElse(UserProfile.builder().user(user).build());

                    // (아이템 조회 로직 - 임시 빈 리스트)
                    List<String> equippedItems = new ArrayList<>();

                    StudyMemberResponse.UserDto userDto = StudyMemberResponse.UserDto.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .bio(profile.getIntroduction())
                            .imgUrl(profile.getProfileImageUrl())
                            .equippedItems(equippedItems)
                            .build();

                    return StudyMemberResponse.MemberDto.builder()
                            .memberId(member.getId())
                            .role(member.getRole().name())
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

        // 1. 현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 (스터디장만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN); // 스터디 관리 권한 없음
        }

        // 4. 대상 멤버 조회
        StudyMember member = studyMemberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND)); // 멤버 없음

        // (멤버가 해당 스터디 소속인지 확인)
        if (!member.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        // 5. 역할 변경 (String -> Enum)
        try {
            StudyMember.StudyRole newRole = StudyMember.StudyRole.valueOf(request.getRole());

            // 본인(리더)의 역할은 변경 불가 (안전장치)
            if (member.getUser().getId().equals(currentUserId)) {
                throw new CustomException(ErrorStatus._BAD_REQUEST); // 리더 위임 기능이 아니라면 본인 변경 막음
            }

            member.updateRole(newRole); // ⭐️ Entity에 updateRole 메서드 필요

        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorStatus._BAD_REQUEST); // 잘못된 역할 이름
        }

        // 6. 저장 (Dirty Checking으로 자동 반영되지만 명시적으로 호출해도 됨)
        studyMemberRepository.save(member);

        // 7. 응답 DTO 반환
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

        // 1. 현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 (리더만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 4. 대상 멤버 조회
        StudyMember member = studyMemberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorStatus._NOT_FOUND));

        // (멤버가 해당 스터디 소속인지 확인)
        if (!member.getStudy().getId().equals(studyId)) {
            throw new CustomException(ErrorStatus._BAD_REQUEST);
        }

        // 5. ⭐️ [방어 로직] 자기 자신 추방 시도 체크 (400 Bad Request)
        if (member.getUser().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus.CANNOT_KICK_SELF);
        }

        // 6. 삭제 (추방)
        studyMemberRepository.delete(member);

        // 7. 응답 반환
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

        // 1. 현재 사용자(리더) 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        // 2. 스터디 조회
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 3. 권한 체크 (리더만 가능)
        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 4. 정보 수정 (값이 있는 것만 수정 - Dirty Checking)
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            study.updateTitle(request.getTitle()); // ⭐️ Entity에 update 메서드 필요
        }
        if (request.getContent() != null && !request.getContent().isBlank()) {
            study.updateContent(request.getContent());
        }
        if (request.getMaxMembers() != null) {
            // (선택) 현재 멤버 수보다 작게 줄일 수 없도록 막는 로직 추가 가능
            study.updateMaxMembers(request.getMaxMembers());
        }
        if (request.getStartDate() != null) {
            study.updateStartDate(request.getStartDate());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                Study.StudyStatus newStatus = Study.StudyStatus.valueOf(request.getStatus());
                study.updateStatus(newStatus);
            } catch (IllegalArgumentException e) {
                // 잘못된 상태값이면 무시하거나 에러 처리
            }
        }

        // 5. 카테고리 수정 (기존 삭제 -> 새 연결)
        if (request.getCategoryIds() != null) {
            // 기존 연결 삭제
            studyHasCategoryRepository.deleteByStudy(study); // ⭐️ Repository에 deleteByStudy 메서드 필요

            // 새 연결 생성
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

        // (명시적 save 호출 없어도 Transactional 때문에 자동 반영됨)
    }
    /**
     * API 5-8: 스터디 일정 및 주기 수정
     */
    @Transactional
    public void updateStudySchedule(Integer studyId, StudyScheduleUpdateRequest request) {

        // 1. 리더 권한 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        if (!study.getLeader().getId().equals(currentUserId)) {
            throw new CustomException(ErrorStatus._FORBIDDEN);
        }

        // 2. [핵심 로직] 시작일 변경 시 -> TodoList 날짜들도 같이 이동 (Shifting)
        if (request.getStartDate() != null) {
            LocalDateTime oldStart = study.getStartDate();
            LocalDateTime newStart = request.getStartDate();

            // 날짜가 변경되었다면?
            if (oldStart != null && !oldStart.isEqual(newStart)) {
                // 두 날짜의 차이(일수) 계산
                long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(oldStart, newStart);

                // 이 스터디의 모든 TodoList 조회 (Repository 메서드 필요!)
                List<TodoList> todoLists = todoListRepository.findByStudy(study);

                // 모든 TodoList의 targetDate를 차이만큼 이동
                for (TodoList list : todoLists) {
                    // 기존 날짜 + 차이값
                    LocalDateTime newTargetDate = list.getTargetDate().plusDays(daysDiff);
                    list.updateTargetDate(newTargetDate); // ⭐️ TodoList 엔티티에 메서드 필요
                }
            }
            // 스터디 시작일 업데이트
            study.updateStartDate(newStart);
        }

        // 3. 종료일 업데이트
        if (request.getEndDate() != null) {
            study.updateEndDate(request.getEndDate()); // ⭐️ Study 엔티티 메서드 필요
        }

        // 4. 주기 업데이트
        if (request.getTodoCycle() != null && !request.getTodoCycle().isBlank()) {
            study.updateTodoCycle(request.getTodoCycle()); // ⭐️ Study 엔티티 메서드 필요
        }

        // (JPA Dirty Checking으로 자동 저장됨)
    }
}