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


    public List<CategoryResponse> getAllCategories() {
        return studyCategoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * API 1-5: 스터디 목록 조회 (최종 수정: 정렬 방어, 다중 키워드, 작성일 추가)
     */
    public StudyListResponse getStudyList(StudyListRequest request) {

        // 1. 정렬 기준 설정 (sortBy 방어 로직)
        Sort sort;
        if ("members".equals(request.getSortBy())) {
            // 참여 멤버 많은 순 (참고: 현재 멤버 수는 별도 조인이 필요하므로, 일단 maxMembers나 DB구조에 따라 다름)
            // 여기서는 예시로 'maxMembers' 내림차순으로 하겠습니다. (혹은 구현 복잡도상 최신순으로 대체 가능)
            sort = Sort.by(Sort.Direction.DESC, "maxMembers");
        } else {
            // 'latest' 이거나, null 이거나, 이상한 값이 오면 -> 무조건 '최신순' (기본값)
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        // 2. 페이지네이션 생성
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 3. 검색 조건 생성 (다중 키워드 처리 - Specification)
        Specification<Study> spec = (root, query, criteriaBuilder) -> {
            // 기본 조건: 삭제되지 않은 것 등 (필요하면 추가)
            List<Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (콤마로 구분)
            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String[] keywords = request.getKeyword().split(","); // 콤마로 쪼개기
                List<Predicate> keywordPredicates = new ArrayList<>();

                for (String keyword : keywords) {
                    String trimKeyword = keyword.trim(); // 공백 제거
                    // 제목에 포함되거나 OR 내용에 포함되거나
                    Predicate titleLike = criteriaBuilder.like(root.get("title"), "%" + trimKeyword + "%");
                    Predicate contentLike = criteriaBuilder.like(root.get("content"), "%" + trimKeyword + "%");

                    // (제목 OR 내용) 조건을 리스트에 추가
                    keywordPredicates.add(criteriaBuilder.or(titleLike, contentLike));
                }

                // 모든 키워드 조건 중 하나라도 맞으면 OK (OR 연산) -> 만약 AND로 하려면 criteriaBuilder.and 사용
                // 요청사항: "React,TypeScript" -> React 검색되거나 TS 검색되거나 (보통 검색은 OR)
                predicates.add(criteriaBuilder.or(keywordPredicates.toArray(new Predicate[0])));
            }

            // 카테고리 필터링 (기존 로직 유지 원하면 추가 가능)
            // ...

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 4. DB 조회 (Specification 사용)
        // StudyRepository에 JpaSpecificationExecutor 상속 필수!
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
                            .studyType(study.getStudyType().name())
                            .categories(categories)
                            .currentMembers(currentMembers)
                            .maxMembers(study.getMaxMembers())
                            .closedAt(study.getClosedAt().toString())
                            .status(study.getStatus().name())
                            // ⭐️ [추가] 작성일 반환
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
                .content(request.getContent())
                // ⭐️ [수정 2] request.get... 대신 위에서 만든 안전한 'type' 변수를 넣어야 합니다!
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

    @Transactional
    public StudyApplyResponse applyToStudy(Integer studyId, StudyApplyRequest request) {

        // 1. 현재 로그인 사용자 확인 (401 Unauthorized)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User applicant = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR)); // DB 오류 또는 사용자 탈퇴

        // 2. 스터디 존재 확인 및 모집 상태 확인 (404/400)
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new CustomException(ErrorStatus.STUDY_NOT_FOUND));

        // 2-1. 모집 상태 확인 (모집 중이 아니라면 400 Bad Request)
        if (study.getStatus() != Study.StudyStatus.RECRUITING) {
            throw new CustomException(ErrorStatus.RECRUITMENT_CLOSED); // ⭐️ RECRUITMENT_CLOSED 에러 코드를 가정합니다.
        }

        // 3. 중복 신청/멤버 확인 (409 Conflict)

        // 3-1. 이미 확정 멤버인지 확인 (StudyMember 테이블)
        if (studyMemberRepository.existsByUserAndStudy(applicant, study)) {
            throw new CustomException(ErrorStatus.ALREADY_MEMBER_OR_APPLIED); // ⭐️ ALREADY_MEMBER_OR_APPLIED 에러 코드를 가정합니다.
        }

        // 3-2. 이미 PENDING 상태로 신청했는지 확인 (StudyApplication 테이블)
        if (studyApplicationRepository.existsByUserAndStudyAndStatus(applicant, study, "PENDING")) {
            throw new CustomException(ErrorStatus.ALREADY_MEMBER_OR_APPLIED);
        }

        // 4. StudyApplication Entity 생성 및 저장
        StudyApplication newApplication = StudyApplication.builder()
                .study(study)
                .user(applicant)
                // ⭐️ StudyApplication 엔티티 내부에 ApplicationStatus Enum이 있다고 가정합니다.
                .status(StudyApplication.ApplicationStatus.PENDING)
                .message(request.getMessage()) // Optional 메시지
                .build();

        newApplication = studyApplicationRepository.save(newApplication);

        // 5. 응답 DTO 반환 (PENDING 상태와 생성된 ID 반환)
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
}