package teamprojects.demo.service.study;

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
import teamprojects.demo.dto.study.StudyListResponse.PageInfoDto;
import teamprojects.demo.dto.study.StudyListResponse.StudyDto;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.StudyCategory;
import teamprojects.demo.entity.StudyHasCategory;
import teamprojects.demo.entity.StudyMember;
import teamprojects.demo.entity.User;
import teamprojects.demo.global.common.code.status.ErrorStatus;
import teamprojects.demo.global.common.exception.CustomException;
import teamprojects.demo.global.utils.SecurityUtils;
import teamprojects.demo.repository.StudyCategoryRepository;
import teamprojects.demo.repository.StudyHasCategoryRepository;
import teamprojects.demo.repository.StudyMemberRepository;
import teamprojects.demo.repository.StudyRepository;
import teamprojects.demo.repository.UserRepository;
import teamprojects.demo.entity.UserProfile; // 에러 4 해결
import teamprojects.demo.repository.UserProfileRepository; // 에러 1 해결
import teamprojects.demo.repository.StudyApplicationRepository; // 에러 2 해결
import teamprojects.demo.dto.study.StudyDetailResponse; // 에러 3 해결
import teamprojects.demo.dto.study.StudyApplyRequest;   // 에러 2, 4 해결
import teamprojects.demo.dto.study.StudyApplyResponse;  // 에러 1 해결
import teamprojects.demo.entity.StudyApplication;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * API 1-5: 스터디 목록 조회
     */
    public StudyListResponse getStudyList(StudyListRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Study> studyPage;

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            studyPage = studyRepository.findByTitleContainingOrContentContaining(
                    request.getKeyword(), request.getKeyword(), pageable);
        } else {
            studyPage = studyRepository.findAll(pageable);
        }

        List<StudyDto> studyDtos = studyPage.getContent().stream()
                .map(study -> {
                    Integer currentMembers = studyMemberRepository.countByStudy(study);
                    List<String> categories = studyHasCategoryRepository.findByStudy(study).stream()
                            .map(shc -> shc.getStudyCategory().getCategoryName())
                            .collect(Collectors.toList());

                    return StudyDto.builder()
                            .studyId(study.getId())
                            .title(study.getTitle())
                            // ⭐️ [수정 1] Entity가 Enum이므로 .name()을 붙여서 String으로 변환해야 합니다!
                            .studyType(study.getStudyType().name())
                            .categories(categories)
                            .currentMembers(currentMembers)
                            .maxMembers(study.getMaxMembers())
                            .closedAt(study.getClosedAt().toString())
                            // ⭐️ [수정 1] 여기도 .name() 추가!
                            .status(study.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());

        PageInfoDto pageInfoDto = PageInfoDto.builder()
                .page(studyPage.getNumber())
                .size(studyPage.getSize())
                .totalPages(studyPage.getTotalPages())
                .totalElements(studyPage.getTotalElements())
                .build();

        return StudyListResponse.builder()
                .studies(studyDtos)
                .pageInfo(pageInfoDto)
                .build();
    }

    /**
     * API 2-1: 스터디 개설
     */
    @Transactional
    public StudyCreateResponse createStudy(StudyCreateRequest request) {

        // 1. 로그인 사용자 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User leader = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 2. 스터디 생성
        Study newStudy = Study.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .studyType(Study.StudyType.valueOf(request.getStudyType()))
                .maxMembers(request.getMaxMembers())
                .closedAt(request.getClosedAt())
                .startDate(request.getStartDate())
                .status(Study.StudyStatus.RECRUITING)
                .leader(leader)
                .build();

        newStudy = studyRepository.save(newStudy);

        // 3. 카테고리 연결
        for (Integer categoryId : request.getCategoryIds()) {
            StudyCategory category = studyCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(ErrorStatus.CATEGORY_NOT_FOUND));

            StudyHasCategory link = StudyHasCategory.builder()
                    .study(newStudy)
                    .studyCategory(category)
                    .build();

            studyHasCategoryRepository.save(link);
        }

        // 4. 스터디장 멤버 등록
        StudyMember leaderMember = StudyMember.builder()
                .study(newStudy)
                .user(leader)
                .role(StudyMember.StudyRole.LEADER)
                // ⭐️ [수정 2] 필드명 불일치 해결 (created -> createdAt)
                // 하지만 @CreationTimestamp가 엔티티에 있다면 생략해도 자동 생성됩니다.
                // 명시적으로 넣으려면 .createdAt(LocalDateTime.now()) 라고 써야 합니다.
                // 여기선 깔끔하게 생략하겠습니다. (엔티티가 알아서 채워줌)
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

        // 4. 신청 후 승인 대기 중 (APPLIED) - PENDING 상태 확인
        if (studyApplicationRepository.existsByUserAndStudyAndStatus(currentUser, study, "PENDING")) {
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
}