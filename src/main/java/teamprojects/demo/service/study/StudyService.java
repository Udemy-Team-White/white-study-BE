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
}