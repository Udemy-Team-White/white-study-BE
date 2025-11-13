package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.StudyMember;
import teamprojects.demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Integer> {

    //API 1-5
    Integer countByStudy(Study study);

    // API 2-2 User와 Study 객체로 멤버 존재 여부 확인
    boolean existsByUserAndStudy(User user, Study study);

    // (API 3-4: 스터디의 확정 멤버 목록 조회 시 사용)
    List<StudyMember> findByStudy(Study study);

    // (API 4-3: 내 스터디 목록 조회 시 사용)
    List<StudyMember> findByUser(User user);

    // (API 3-6: 멤버 추방 등, 특정 멤버 조회 시 사용)
    Optional<StudyMember> findByIdAndStudy(Long memberId, Study study);

    // (보안: 사용자가 스터디 멤버인지 확인 시 사용)
    Optional<StudyMember> findByUserAndStudy(User user, Study study);
}