// teamprojects.demo.repository.StudyRepository.java

package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // @Query 사용을 위해 추가
import org.springframework.data.repository.query.Param; // @Param 사용을 위해 추가

public interface StudyRepository extends JpaRepository<Study, Integer>,JpaSpecificationExecutor<Study> {

    // (기존 findByTitleContainingOrContentContaining 메서드 유지)
    Page<Study> findByTitleContainingOrContentContaining(String titleKeyword, String contentKeyword, Pageable pageable);

    // ⭐️ API 3-3을 위해 추가: User ID와 상태로 필터링하는 복잡한 쿼리
    // [가정] Study 엔티티에 studyMembers 컬렉션이 있고, StudyMember 엔티티에 user 필드가 있다고 가정합니다.
    @Query("SELECT s FROM Study s JOIN s.studyMembers sm WHERE sm.user.id = :userId AND s.status = :status")
    Page<Study> findStudiesByUserIdAndStatus(@Param("userId") Integer userId, @Param("status") String status, Pageable pageable);
}